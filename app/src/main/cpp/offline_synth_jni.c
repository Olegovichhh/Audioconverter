#include <jni.h>
#include <stdlib.h>
#include <stdint.h>
typedef struct _fluid_settings_t fluid_settings_t;
typedef struct _fluid_synth_t fluid_synth_t;
fluid_settings_t* new_fluid_settings(void);
void delete_fluid_settings(fluid_settings_t*);
int fluid_settings_setnum(fluid_settings_t*,const char*,double);
fluid_synth_t* new_fluid_synth(fluid_settings_t*);
int delete_fluid_synth(fluid_synth_t*);
int fluid_synth_sfload(fluid_synth_t*,const char*,int);
int fluid_synth_noteon(fluid_synth_t*,int,int,int);
int fluid_synth_noteoff(fluid_synth_t*,int,int);
int fluid_synth_program_change(fluid_synth_t*,int,int);
int fluid_synth_write_float(fluid_synth_t*,int,void*,int,int,void*,int,int);
typedef struct{fluid_settings_t*s;fluid_synth_t*y;}ctx;
JNIEXPORT jlong JNICALL Java_com_olegovichhh_audioconverter_OfflineSynth_create(JNIEnv*e,jobject o,jint sr){ctx*c=calloc(1,sizeof(ctx));if(!c)return 0;c->s=new_fluid_settings();if(!c->s){free(c);return 0;}fluid_settings_setnum(c->s,"synth.sample-rate",(double)sr);c->y=new_fluid_synth(c->s);if(!c->y){delete_fluid_settings(c->s);free(c);return 0;}return (jlong)(intptr_t)c;}
JNIEXPORT void JNICALL Java_com_olegovichhh_audioconverter_OfflineSynth_destroy(JNIEnv*e,jobject o,jlong p){ctx*c=(ctx*)(intptr_t)p;if(!c)return;if(c->y)delete_fluid_synth(c->y);if(c->s)delete_fluid_settings(c->s);free(c);}
JNIEXPORT jint JNICALL Java_com_olegovichhh_audioconverter_OfflineSynth_loadSf(JNIEnv*e,jobject o,jlong p,jstring s){ctx*c=(ctx*)(intptr_t)p;const char*x=(*e)->GetStringUTFChars(e,s,0);int r=fluid_synth_sfload(c->y,x,1);(*e)->ReleaseStringUTFChars(e,s,x);return r;}
JNIEXPORT void JNICALL Java_com_olegovichhh_audioconverter_OfflineSynth_noteOn0(JNIEnv*e,jobject o,jlong p,jint c,jint k,jint v){fluid_synth_noteon(((ctx*)(intptr_t)p)->y,c,k,v);}
JNIEXPORT void JNICALL Java_com_olegovichhh_audioconverter_OfflineSynth_noteOff0(JNIEnv*e,jobject o,jlong p,jint c,jint k){fluid_synth_noteoff(((ctx*)(intptr_t)p)->y,c,k);}
JNIEXPORT void JNICALL Java_com_olegovichhh_audioconverter_OfflineSynth_program0(JNIEnv*e,jobject o,jlong p,jint c,jint v){fluid_synth_program_change(((ctx*)(intptr_t)p)->y,c,v);}
JNIEXPORT jfloatArray JNICALL Java_com_olegovichhh_audioconverter_OfflineSynth_render0(JNIEnv*e,jobject o,jlong p,jint n){ctx*c=(ctx*)(intptr_t)p;if(!c||!c->y||n<=0)return NULL;float*l=calloc(n,sizeof(float));float*r=calloc(n,sizeof(float));float*b=malloc((size_t)n*2*sizeof(float));if(!l||!r||!b){free(l);free(r);free(b);return NULL;}fluid_synth_write_float(c->y,n,l,0,1,r,0,1);for(int i=0;i<n;i++){b[i*2]=l[i];b[i*2+1]=r[i];}jfloatArray a=(*e)->NewFloatArray(e,n*2);if(a)(*e)->SetFloatArrayRegion(e,a,0,n*2,b);free(l);free(r);free(b);return a;}