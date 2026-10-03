package com.olegovichhh.audioconverter
import java.io.*
import kotlin.math.*

object MidiRenderer {
 data class E(val tick:Long,val type:Int,val ch:Int=0,val a:Int=0,val b:Int=0,val tempo:Int=500000)
 fun render(midi:File,sf2:File,wav:File,sr:Int=44100,onProgress:(Int)->Unit={}){
  val bytes=midi.readBytes(); val ppq=((bytes[12].toInt() and 255) shl 8) or (bytes[13].toInt() and 255)
  require(ppq and 0x8000==0){"SMPTE MIDI пока не поддерживается"}
  val ev=mutableListOf<E>(); var p=14
  while(p+8<=bytes.size&&String(bytes,p,4)=="MTrk"){val len=i32(bytes,p+4);val end=p+8+len;p+=8;var tick=0L;var run=0
   while(p<end){val(v,np)=vlq(bytes,p);p=np;tick+=v;var st=bytes[p].toInt() and 255;if(st<128)st=run else{p++;if(st<240)run=st}
    when{st==0xFF->{val mt=bytes[p++].toInt() and 255;val(l,n)=vlq(bytes,p);p=n;if(mt==0x51&&l==3L){val t=((bytes[p].toInt()and 255)shl 16)or((bytes[p+1].toInt()and 255)shl 8)or(bytes[p+2].toInt()and 255);ev+=E(tick,3,tempo=t)};p+=l.toInt()}
     st==0xF0||st==0xF7->{val(l,n)=vlq(bytes,p);p=n+l.toInt()}
     else->{val hi=st and 0xF0;val ch=st and 15;val a=bytes[p++].toInt()and 255;val b=if(hi==0xC0||hi==0xD0)0 else bytes[p++].toInt()and 255
      when(hi){0x80->ev+=E(tick,1,ch,a,b);0x90->ev+=E(tick,if(b==0)1 else 0,ch,a,b);0xC0->ev+=E(tick,2,ch,a)}}}
   };p=end
  }
  ev.sortWith(compareBy<E>{it.tick}.thenBy{it.type});val synth=OfflineSynth(sr);require(synth.load(sf2.absolutePath)>=0){"Не удалось загрузить SoundFont"}
  val fos=BufferedOutputStream(FileOutputStream(wav),262144);fos.write(ByteArray(44));var pcmBytes=0L;var last=0L;var tempo=500000;val maxTick=(ev.lastOrNull()?.tick?:1L).coerceAtLeast(1L);var lastPct=-1
  fun audioTo(t:Long){val dt=t-last;if(dt<=0)return;val raw=dt.toDouble()*tempo*sr/(ppq*1_000_000.0);require(raw<sr*60.0*30){"Слишком большой разрыв между MIDI-событиями"};var frames=raw.roundToInt();while(frames>0){val n=min(8192,frames);val a=synth.render(n);val bb=ByteArray(a.size*2);var j=0;for(x in a){val q=(x.coerceIn(-1f,1f)*32767f).roundToInt();bb[j++]=(q and 255).toByte();bb[j++]=((q shr 8)and 255).toByte()};fos.write(bb);pcmBytes+=bb.size;frames-=n};last=t}
  for(e in ev){audioTo(e.tick);when(e.type){0->synth.noteOn(e.ch,e.a,e.b);1->synth.noteOff(e.ch,e.a);2->synth.program(e.ch,e.a);3->tempo=e.tempo};val pct=(e.tick*95/maxTick).toInt();if(pct!=lastPct){lastPct=pct;onProgress(pct)}}
  var tail=sr*2;while(tail>0){val n=min(8192,tail);val a=synth.render(n);val bb=ByteArray(a.size*2);var j=0;for(x in a){val q=(x.coerceIn(-1f,1f)*32767f).roundToInt();bb[j++]=(q and 255).toByte();bb[j++]=((q shr 8)and 255).toByte()};fos.write(bb);pcmBytes+=bb.size;tail-=n};fos.flush();fos.close();patchWav(wav,pcmBytes,sr);onProgress(100)
 }
 private fun i32(b:ByteArray,p:Int)=((b[p].toInt()and 255)shl 24)or((b[p+1].toInt()and 255)shl 16)or((b[p+2].toInt()and 255)shl 8)or(b[p+3].toInt()and 255)
 private fun vlq(b:ByteArray,s:Int):Pair<Long,Int>{var p=s;var v=0L;do{val x=b[p++].toInt()and 255;v=(v shl 7)or(x and 127).toLong()}while(x and 128!=0);return v to p}
 private fun patchWav(f:File,bytes:Long,sr:Int){RandomAccessFile(f,"rw").use{o->fun le(v:Int){o.writeByte(v);o.writeByte(v shr 8);o.writeByte(v shr 16);o.writeByte(v shr 24)};fun s(v:Int){o.writeByte(v);o.writeByte(v shr 8)};o.seek(0);o.writeBytes("RIFF");le((36+bytes).toInt());o.writeBytes("WAVEfmt ");le(16);s(1);s(2);le(sr);le(sr*4);s(4);s(16);o.writeBytes("data");le(bytes.toInt())}}
}