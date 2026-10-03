package com.olegovichhh.audioconverter
class OfflineSynth(sampleRate:Int=44100){
 private var ptr:Long
 init{System.loadLibrary("offline_synth_jni");ptr=create(sampleRate);require(ptr!=0L){"Offline FluidSynth init failed"}}
 fun load(path:String)=loadSf(ptr,path)
 fun noteOn(ch:Int,key:Int,vel:Int)=noteOn0(ptr,ch,key,vel)
 fun noteOff(ch:Int,key:Int)=noteOff0(ptr,ch,key)
 fun program(ch:Int,p:Int)=program0(ptr,ch,p)
 fun render(frames:Int)=render0(ptr,frames)
 fun close(){if(ptr!=0L){destroy(ptr);ptr=0}}
 private external fun create(sr:Int):Long
 private external fun destroy(p:Long)
 private external fun loadSf(p:Long,path:String):Int
 private external fun noteOn0(p:Long,ch:Int,key:Int,vel:Int)
 private external fun noteOff0(p:Long,ch:Int,key:Int)
 private external fun program0(p:Long,ch:Int,program:Int)
 private external fun render0(p:Long,frames:Int):FloatArray
}