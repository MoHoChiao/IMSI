/* Generated by Nim Compiler v0.18.0 */
/*   (c) 2018 Andreas Rumpf */
/* The generated code is subject to the original license. */
/* Compiled for: Linux, amd64, gcc */
/* Command for C compiler:
   gcc -c  -w -O3 -fno-strict-aliasing  -I/root/.choosenim/toolchains/nim-0.18.0/lib -o /mnt/d/Development/bitbucket/java/NetScoutMerger/script/lsrreport/nimcache/stdlib_times.o /mnt/d/Development/bitbucket/java/NetScoutMerger/script/lsrreport/nimcache/stdlib_times.c */
#define NIM_NEW_MANGLING_RULES
#define NIM_INTBITS 64

#include "nimbase.h"
#include <time.h>
#include <sys/select.h>
#include <string.h>
#include <sys/time.h>
#undef LANGUAGE_C
#undef MIPSEB
#undef MIPSEL
#undef PPC
#undef R3000
#undef R4000
#undef i386
#undef linux
#undef mips
#undef near
#undef powerpc
#undef unix

N_LIB_PRIVATE N_NIMCALL(NI64, getTime_wqmoaGqnr3gMTpWruGQ3FA)(void) {
	NI64 result;
	long T1_;
	result = (NI64)0;
	T1_ = (long)0;
	T1_ = time(NIM_NIL);
	result = ((NI64) (T1_));
	return result;
}

N_LIB_PRIVATE N_NIMCALL(NI64, fromUnix_C77WZNbbZYep1XVMN2QCfA)(NI64 unix) {
	NI64 result;
	result = (NI64)0;
	result = unix;
	return result;
}

N_LIB_PRIVATE N_NIMCALL(NI64, toUnix_KOVzgXVRJxxlPUOsrk3eDQ)(NI64 t) {
	NI64 result;
	result = (NI64)0;
	result = t;
	return result;
}

N_LIB_PRIVATE N_NIMCALL(NF, ntepochTime)(void) {
	NF result;
	struct timeval a;
	result = (NF)0;
	memset((void*)(&a), 0, sizeof(a));
	gettimeofday((&a), NIM_NIL);
	result = ((NF)(((double) (a.tv_sec))) + (NF)(((NF)(((double) (a.tv_usec))) * (NF)(9.9999999999999995e-07))));
	return result;
}
NIM_EXTERNC N_NOINLINE(void, stdlib_timesInit000)(void) {
	tzset();
}

NIM_EXTERNC N_NOINLINE(void, stdlib_timesDatInit000)(void) {
}

