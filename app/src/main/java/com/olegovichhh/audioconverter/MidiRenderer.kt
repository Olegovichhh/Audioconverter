package com.olegovichhh.audioconverter
import dev.kotlinds.fluidsynthkmp.AudioConfig
import dev.kotlinds.fluidsynthkmp.FluidSynthPlayer
import java.io.*
import kotlin.math.*

object MidiRenderer {
 data class E(val tick:Long,val type:Int,val ch:Int=0,val a:Int=0,val b:Int=0,val tempo:Int=500000)
 fun render(midi:File,sf2:File,wav:File,sr:Int=44100){
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
  ev.sortWith(compareBy<E>{it.tick}.thenBy{it.type});val synth=FluidSynthPlayer(AudioConfig(sampleRate=sr));require(synth.loadSoundFont(sf2.absolutePath)>=0){"Не удалось загрузить SoundFont"}
  val out=ByteArrayOutputStream();var last=0L;var tempo=500000
  fun audioTo(t:Long){val dt=t-last;if(dt<=0)return;var frames=(dt.toDouble()*tempo*sr/(ppq*1_000_000.0)).roundToInt();while(frames>0){val n=min(2048,frames);val f=synth.renderFloat(n);for(x in f){val q=(x.coerceIn(-1f,1f)*32767f).roundToInt();out.write(q and 255);out.write((q shr 8)and 255)};frames-=n};last=t}
  for(e in ev){audioTo(e.tick);when(e.type){0->synth.noteOn(e.ch,e.a,e.b);1->synth.noteOff(e.ch,e.a);2->synth.programChange(e.ch,e.a);3->tempo=e.tempo}}
  var tail=sr*2;while(tail>0){val n=min(2048,tail);for(x in synth.renderFloat(n)){val q=(x.coerceIn(-1f,1f)*32767f).roundToInt();out.write(q and 255);out.write((q shr 8)and 255)};tail-=n};synth.close();writeWav(wav,out.toByteArray(),sr)
 }
 private fun i32(b:ByteArray,p:Int)=((b[p].toInt()and 255)shl 24)or((b[p+1].toInt()and 255)shl 16)or((b[p+2].toInt()and 255)shl 8)or(b[p+3].toInt()and 255)
 private fun vlq(b:ByteArray,s:Int):Pair<Long,Int>{var p=s;var v=0L;do{val x=b[p++].toInt()and 255;v=(v shl 7)or(x and 127).toLong()}while(x and 128!=0);return v to p}
 private fun writeWav(f:File,pcm:ByteArray,sr:Int){DataOutputStream(FileOutputStream(f)).use{o->fun le(v:Int){o.writeByte(v);o.writeByte(v shr 8);o.writeByte(v shr 16);o.writeByte(v shr 24)};fun s(v:Int){o.writeByte(v);o.writeByte(v shr 8)};o.writeBytes("RIFF");le(36+pcm.size);o.writeBytes("WAVEfmt ");le(16);s(1);s(2);le(sr);le(sr*4);s(4);s(16);o.writeBytes("data");le(pcm.size);o.write(pcm)}}
}