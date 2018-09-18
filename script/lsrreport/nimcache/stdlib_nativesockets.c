/* Generated by Nim Compiler v0.18.0 */
/*   (c) 2018 Andreas Rumpf */
/* The generated code is subject to the original license. */
/* Compiled for: Linux, amd64, gcc */
/* Command for C compiler:
   gcc -c  -w -O3 -fno-strict-aliasing  -I/root/.choosenim/toolchains/nim-0.18.0/lib -o /mnt/d/Development/bitbucket/java/NetScoutMerger/script/lsrreport/nimcache/stdlib_nativesockets.o /mnt/d/Development/bitbucket/java/NetScoutMerger/script/lsrreport/nimcache/stdlib_nativesockets.c */
#define NIM_NEW_MANGLING_RULES
#define NIM_INTBITS 64

#include "nimbase.h"
#include <sys/socket.h>
#include <netdb.h>
#include <string.h>
#include <sys/select.h>
#include <unistd.h>
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
typedef struct TNimType TNimType;
typedef struct TNimNode TNimNode;
typedef struct NimStringDesc NimStringDesc;
typedef struct TGenericSeq TGenericSeq;
typedef struct tySequence_9apztJSmgERYU8fZOjI4pOg tySequence_9apztJSmgERYU8fZOjI4pOg;
typedef NU8 tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg;
typedef NU8 tyEnum_TNimKind_jIBKr1ejBgsfM33Kxw4j7A;
typedef NU8 tySet_tyEnum_TNimTypeFlag_v8QUszD1sWlSIWZz7mC4bQ;
typedef N_NIMCALL_PTR(void, tyProc_ojoeKfW4VYIm36I9cpDTQIg) (void* p, NI op);
typedef N_NIMCALL_PTR(void*, tyProc_WSm2xU5ARYv9aAR4l0z9c9auQ) (void* p);
struct TNimType {
NI size;
tyEnum_TNimKind_jIBKr1ejBgsfM33Kxw4j7A kind;
tySet_tyEnum_TNimTypeFlag_v8QUszD1sWlSIWZz7mC4bQ flags;
TNimType* base;
TNimNode* node;
void* finalizer;
tyProc_ojoeKfW4VYIm36I9cpDTQIg marker;
tyProc_WSm2xU5ARYv9aAR4l0z9c9auQ deepcopy;
};
typedef NU8 tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw;
typedef NU8 tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg;
struct TGenericSeq {
NI len;
NI reserved;
};
struct NimStringDesc {
  TGenericSeq Sup;
NIM_CHAR data[SEQ_DECL_SIZE];
};
typedef long tyArray_RpaqwQ7H8ofV6NGQYsCBHQ[16];
typedef NU8 tyEnum_TNimTypeFlag_v8QUszD1sWlSIWZz7mC4bQ;
typedef NU8 tyEnum_WalkOp_Wfy7gT5VQ8B3aJBxqU8D9cQ;
typedef NU8 tyEnum_TNimNodeKind_unfNsxrcATrufDZmpBq4HQ;
struct TNimNode {
tyEnum_TNimNodeKind_unfNsxrcATrufDZmpBq4HQ kind;
NI offset;
TNimType* typ;
NCSTRING name;
NI len;
TNimNode** sons;
};
typedef NIM_CHAR tyArray_NSMq3FMCIrS8gSbyinBZ8w[14];
struct tySequence_9apztJSmgERYU8fZOjI4pOg {
  TGenericSeq Sup;
  int data[SEQ_DECL_SIZE];
};
N_LIB_PRIVATE N_NIMCALL(short, toInt_OxnVJkB3zYuSbbBvLQDSVA)(tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg domain);
N_LIB_PRIVATE N_NIMCALL(int, toInt_8FqW9bXNjjJADB8XlKnpNLg)(tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw typ);
N_LIB_PRIVATE N_NIMCALL(int, toInt_9cDpnbeIxivYaLcJBJkQ2nQ)(tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg p);
N_LIB_PRIVATE N_NIMCALL(NimStringDesc*, dollar__rzAI8EMyNBAQwGODeohhAA)(NU64 x);
N_LIB_PRIVATE N_NOINLINE(void, raiseOSError_bEwAamo1N7TKcaU9akpiyIg)(NI32 errorCode, NimStringDesc* additionalInfo);
N_LIB_PRIVATE N_NIMCALL(NI32, osLastError_tNPXXFl9cb3BG0pJKzUn9bew)(void);
N_NIMCALL(NimStringDesc*, cstrToNimstr)(NCSTRING str);
N_LIB_PRIVATE N_NIMCALL(struct timeval, timeValFromMilliseconds_X7JbWdPaNDQPHMVB9aPz9bkQ)(NI timeout);
N_LIB_PRIVATE N_NIMCALL(void, createFdSet_2WN0c6XLlKabdgianu73bg)(fd_set* fd, tySequence_9apztJSmgERYU8fZOjI4pOg* s, NI* m);
N_LIB_PRIVATE N_NIMCALL(void, pruneSocketSet_1LwJOFdSmq6EdasZp5EGUA)(tySequence_9apztJSmgERYU8fZOjI4pOg** s, fd_set* fd);
static N_INLINE(TGenericSeq*, setLengthSeq)(TGenericSeq* seq, NI elemSize, NI newLen);
static N_INLINE(NI, resize_bzF9a0JivP3Z4njqaxuLc5wsystem)(NI old);
N_LIB_PRIVATE N_NIMCALL(void*, growObj_AVYny8c0GTk28gxjmat1MA)(void* old, NI newsize);
N_NIMCALL(TNimType*, extGetCellType)(void* c);
N_LIB_PRIVATE N_NIMCALL(void, forAllChildrenAux_YOI1Uo54H9aas13WthjhsfA)(void* dest, TNimType* mt, tyEnum_WalkOp_Wfy7gT5VQ8B3aJBxqU8D9cQ op);
static N_INLINE(void, zeroMem_t0o5XqKanO5QJfXMGEzp2Asystem)(void* p, NI size);
int osInvalidSocket_voz9aUXu8jtRbvGZZJHNE8w;
sa_family_t nativeAfInet_rQwsjQjVqXvdaL9aZofzWwg;
sa_family_t nativeAfInet6_Da6PongZL9aJxBrf7qeBmfA;
TNimType NTI_Q79bEtFARvq0ekDNtvj3Vqg_;
TNimType NTI_NQT1bItGG2X9byGdrWX7ujw_;
TNimType NTI_dqJ1OqRGclxIMMdSLRzzXg_;

N_LIB_PRIVATE N_NIMCALL(short, toInt_OxnVJkB3zYuSbbBvLQDSVA)(tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg domain) {
	short result;
	result = (short)0;
	switch (domain) {
	case ((tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg) 0):
	{
		result = ((short) 0);
	}
	break;
	case ((tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg) 1):
	{
		result = ((short) 1);
	}
	break;
	case ((tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg) 2):
	{
		result = ((short) 2);
	}
	break;
	case ((tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg) 23):
	{
		result = ((short) 10);
	}
	break;
	}
	return result;
}

N_LIB_PRIVATE N_NIMCALL(int, toInt_8FqW9bXNjjJADB8XlKnpNLg)(tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw typ) {
	int result;
	result = (int)0;
	switch (typ) {
	case ((tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw) 1):
	{
		result = ((int) 1);
	}
	break;
	case ((tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw) 2):
	{
		result = ((int) 2);
	}
	break;
	case ((tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw) 5):
	{
		result = ((int) 5);
	}
	break;
	case ((tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw) 3):
	{
		result = ((int) 3);
	}
	break;
	}
	return result;
}

N_LIB_PRIVATE N_NIMCALL(int, toInt_9cDpnbeIxivYaLcJBJkQ2nQ)(tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg p) {
	int result;
	result = (int)0;
	switch (p) {
	case ((tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg) 6):
	{
		result = ((int) 6);
	}
	break;
	case ((tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg) 17):
	{
		result = ((int) 17);
	}
	break;
	case ((tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg) 18):
	{
		result = ((int) 0);
	}
	break;
	case ((tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg) 19):
	{
		result = ((int) 41);
	}
	break;
	case ((tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg) 20):
	{
		result = ((int) 255);
	}
	break;
	case ((tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg) 21):
	{
		result = ((int) 1);
	}
	break;
	}
	return result;
}

N_LIB_PRIVATE N_NIMCALL(int, createNativeSocket_BcG9aGQhTam9ajUKte8LeMdQ)(tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg domain, tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw sockType, tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg protocol) {
	int result;
	short T1_;
	int T2_;
	int T3_;
	result = (int)0;
	T1_ = (short)0;
	T1_ = toInt_OxnVJkB3zYuSbbBvLQDSVA(domain);
	T2_ = (int)0;
	T2_ = toInt_8FqW9bXNjjJADB8XlKnpNLg(sockType);
	T3_ = (int)0;
	T3_ = toInt_9cDpnbeIxivYaLcJBJkQ2nQ(protocol);
	result = socket(((int) (T1_)), T2_, T3_);
	return result;
}

N_LIB_PRIVATE N_NIMCALL(struct addrinfo*, getAddrInfo_fNPnGAYqSzdc2OjHEcTTAw)(NimStringDesc* address, NU16 port, tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg domain, tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw sockType, tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg protocol) {
	struct addrinfo* result;
	struct addrinfo hints;
	short T1_;
	int gaiResult;
	NimStringDesc* T6_;
	result = (struct addrinfo*)0;
	memset((void*)(&hints), 0, sizeof(hints));
	result = NIM_NIL;
	T1_ = (short)0;
	T1_ = toInt_OxnVJkB3zYuSbbBvLQDSVA(domain);
	hints.ai_family = ((int) (T1_));
	hints.ai_socktype = toInt_8FqW9bXNjjJADB8XlKnpNLg(sockType);
	hints.ai_protocol = toInt_9cDpnbeIxivYaLcJBJkQ2nQ(protocol);
	{
		if (!(domain == ((tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg) 23))) goto LA4_;
		hints.ai_flags = ((int) 8);
	}
	LA4_: ;
	T6_ = (NimStringDesc*)0;
	T6_ = dollar__rzAI8EMyNBAQwGODeohhAA(port);
	gaiResult = getaddrinfo(address->data, T6_->data, (&hints), &result);
	{
		NI32 T11_;
		NCSTRING T12_;
		NimStringDesc* T13_;
		if (!!((gaiResult == ((NI32) 0)))) goto LA9_;
		T11_ = (NI32)0;
		T11_ = osLastError_tNPXXFl9cb3BG0pJKzUn9bew();
		T12_ = (NCSTRING)0;
		T12_ = (char *)gai_strerror(gaiResult);
		T13_ = (NimStringDesc*)0;
		T13_ = cstrToNimstr(T12_);
		raiseOSError_bEwAamo1N7TKcaU9akpiyIg(T11_, T13_);
	}
	LA9_: ;
	return result;
}

N_LIB_PRIVATE N_NIMCALL(struct timeval, timeValFromMilliseconds_X7JbWdPaNDQPHMVB9aPz9bkQ)(NI timeout) {
	struct timeval result;
	memset((void*)(&result), 0, sizeof(result));
	{
		NI seconds;
		if (!!((timeout == ((NI) -1)))) goto LA3_;
		seconds = (NI)(timeout / ((NI) 1000));
		result.tv_sec = ((long) (((NI32) (seconds))));
		result.tv_usec = ((long) (((NI32) ((NI)((NI)(timeout - (NI)(seconds * ((NI) 1000))) * ((NI) 1000))))));
	}
	LA3_: ;
	return result;
}

N_LIB_PRIVATE N_NIMCALL(void, createFdSet_2WN0c6XLlKabdgianu73bg)(fd_set* fd, tySequence_9apztJSmgERYU8fZOjI4pOg* s, NI* m) {
	FD_ZERO(fd);
	{
		int i;
		NI i_2;
		NI L;
		NI T2_;
		i = (int)0;
		i_2 = ((NI) 0);
		T2_ = (s ? s->Sup.len : 0);
		L = T2_;
		{
			while (1) {
				if (!(i_2 < L)) goto LA4;
				i = s->data[i_2];
				(*m) = (((*m) >= ((NI) (i))) ? (*m) : ((NI) (i)));
				FD_SET(i, fd);
				i_2 += ((NI) 1);
			} LA4: ;
		}
	}
}

static N_INLINE(NI, resize_bzF9a0JivP3Z4njqaxuLc5wsystem)(NI old) {
	NI result;
	result = (NI)0;
	{
		if (!(old <= ((NI) 0))) goto LA3_;
		result = ((NI) 4);
	}
	goto LA1_;
	LA3_: ;
	{
		if (!(old < ((NI) 65536))) goto LA6_;
		result = (NI)(old * ((NI) 2));
	}
	goto LA1_;
	LA6_: ;
	{
		result = (NI)((NI)(old * ((NI) 3)) / ((NI) 2));
	}
	LA1_: ;
	return result;
}

static N_INLINE(void, zeroMem_t0o5XqKanO5QJfXMGEzp2Asystem)(void* p, NI size) {
	void* T1_;
	T1_ = (void*)0;
	T1_ = memset(p, ((int) 0), ((size_t) (size)));
}

static N_INLINE(TGenericSeq*, setLengthSeq)(TGenericSeq* seq, NI elemSize, NI newLen) {
	TGenericSeq* result;
	result = (TGenericSeq*)0;
	result = seq;
	{
		NI r;
		NI T5_;
		void* T6_;
		if (!((NI)((*result).reserved & ((NI) IL64(4611686018427387903))) < newLen)) goto LA3_;
		T5_ = (NI)0;
		T5_ = resize_bzF9a0JivP3Z4njqaxuLc5wsystem((NI)((*result).reserved & ((NI) IL64(4611686018427387903))));
		r = ((T5_ >= newLen) ? T5_ : newLen);
		T6_ = (void*)0;
		T6_ = growObj_AVYny8c0GTk28gxjmat1MA(((void*) (result)), (NI)((NI)(elemSize * r) + ((NI) 16)));
		result = ((TGenericSeq*) (T6_));
		(*result).reserved = r;
	}
	goto LA1_;
	LA3_: ;
	{
		if (!(newLen < (*result).len)) goto LA8_;
		{
			TNimType* T12_;
			T12_ = (TNimType*)0;
			T12_ = extGetCellType(((void*) (result)));
			if (!!((((*(*T12_).base).flags &(1U<<((NU)(((tyEnum_TNimTypeFlag_v8QUszD1sWlSIWZz7mC4bQ) 0))&7U)))!=0))) goto LA13_;
			{
				NI i;
				NI colontmp_;
				NI res;
				i = (NI)0;
				colontmp_ = (NI)0;
				colontmp_ = (NI)((*result).len - ((NI) 1));
				res = newLen;
				{
					while (1) {
						TNimType* T18_;
						if (!(res <= colontmp_)) goto LA17;
						i = res;
						T18_ = (TNimType*)0;
						T18_ = extGetCellType(((void*) (result)));
						forAllChildrenAux_YOI1Uo54H9aas13WthjhsfA(((void*) ((NI)((NU64)((NI)((NU64)(((NI) (ptrdiff_t) (result))) + (NU64)(((NI) 16)))) + (NU64)((NI)((NU64)(i) * (NU64)(elemSize)))))), (*T18_).base, ((tyEnum_WalkOp_Wfy7gT5VQ8B3aJBxqU8D9cQ) 2));
						res += ((NI) 1);
					} LA17: ;
				}
			}
		}
		LA13_: ;
		zeroMem_t0o5XqKanO5QJfXMGEzp2Asystem(((void*) ((NI)((NU64)((NI)((NU64)(((NI) (ptrdiff_t) (result))) + (NU64)(((NI) 16)))) + (NU64)((NI)((NU64)(newLen) * (NU64)(elemSize)))))), ((NI) ((NI)((NU64)((NI)((NU64)((*result).len) - (NU64)(newLen))) * (NU64)(elemSize)))));
	}
	goto LA1_;
	LA8_: ;
	LA1_: ;
	(*result).len = newLen;
	return result;
}

N_LIB_PRIVATE N_NIMCALL(void, pruneSocketSet_1LwJOFdSmq6EdasZp5EGUA)(tySequence_9apztJSmgERYU8fZOjI4pOg** s, fd_set* fd) {
	NI i;
	NI L;
	NI T1_;
	i = ((NI) 0);
	T1_ = ((*s) ? (*s)->Sup.len : 0);
	L = T1_;
	{
		while (1) {
			if (!(i < L)) goto LA3;
			{
				int T6_;
				T6_ = (int)0;
				T6_ = FD_ISSET((*s)->data[i], fd);
				if (!(T6_ == ((NI32) 0))) goto LA7_;
				(*s)->data[i] = (*s)->data[(NI)(L - ((NI) 1))];
				L -= ((NI) 1);
			}
			goto LA4_;
			LA7_: ;
			{
				i += ((NI) 1);
			}
			LA4_: ;
		} LA3: ;
	}
	(*s) = (tySequence_9apztJSmgERYU8fZOjI4pOg*) setLengthSeq(&((*s))->Sup, sizeof(int), ((NI) (L)));
}

N_LIB_PRIVATE N_NIMCALL(NI, select_f1UV2yno269c9cyjR0CwiQwg)(tySequence_9apztJSmgERYU8fZOjI4pOg** readfds, NI timeout) {
	NI result;
	struct timeval tv;
	fd_set rd;
	NI m;
	result = (NI)0;
	tv = timeValFromMilliseconds_X7JbWdPaNDQPHMVB9aPz9bkQ(timeout);
	memset((void*)(&rd), 0, sizeof(rd));
	m = ((NI) 0);
	createFdSet_2WN0c6XLlKabdgianu73bg((&rd), (*readfds), (&m));
	{
		int T5_;
		if (!!((timeout == ((NI) -1)))) goto LA3_;
		T5_ = (int)0;
		T5_ = select(((int) ((NI)(m + ((NI) 1)))), (&rd), NIM_NIL, NIM_NIL, (&tv));
		result = ((NI) (T5_));
	}
	goto LA1_;
	LA3_: ;
	{
		int T7_;
		T7_ = (int)0;
		T7_ = select(((int) ((NI)(m + ((NI) 1)))), (&rd), NIM_NIL, NIM_NIL, NIM_NIL);
		result = ((NI) (T7_));
	}
	LA1_: ;
	pruneSocketSet_1LwJOFdSmq6EdasZp5EGUA(readfds, (&rd));
	return result;
}

N_LIB_PRIVATE N_NIMCALL(void, close_ODRNWwddsp7NVetdw9cXCVg)(int socket) {
	int T1_;
	T1_ = (int)0;
	T1_ = close(socket);
	T1_;
}
NIM_EXTERNC N_NOINLINE(void, stdlib_nativesocketsInit000)(void) {
	osInvalidSocket_voz9aUXu8jtRbvGZZJHNE8w = ((int) -1);
	nativeAfInet_rQwsjQjVqXvdaL9aZofzWwg = ((sa_family_t) 2);
	nativeAfInet6_Da6PongZL9aJxBrf7qeBmfA = ((sa_family_t) 10);
}

NIM_EXTERNC N_NOINLINE(void, stdlib_nativesocketsDatInit000)(void) {
static TNimNode* TM_f9bP3LqjpgpB9cXL8Nnak7tQ_2[4];
NI TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4;
static char* NIM_CONST TM_f9bP3LqjpgpB9cXL8Nnak7tQ_3[4] = {
"AF_UNSPEC", 
"AF_UNIX", 
"AF_INET", 
"AF_INET6"};
static TNimNode* TM_f9bP3LqjpgpB9cXL8Nnak7tQ_5[4];
NI TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7;
static char* NIM_CONST TM_f9bP3LqjpgpB9cXL8Nnak7tQ_6[4] = {
"SOCK_STREAM", 
"SOCK_DGRAM", 
"SOCK_RAW", 
"SOCK_SEQPACKET"};
static TNimNode* TM_f9bP3LqjpgpB9cXL8Nnak7tQ_8[6];
NI TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10;
static char* NIM_CONST TM_f9bP3LqjpgpB9cXL8Nnak7tQ_9[6] = {
"IPPROTO_TCP", 
"IPPROTO_UDP", 
"IPPROTO_IP", 
"IPPROTO_IPV6", 
"IPPROTO_RAW", 
"IPPROTO_ICMP"};
static TNimNode TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[17];
NTI_Q79bEtFARvq0ekDNtvj3Vqg_.size = sizeof(tyEnum_Domain_Q79bEtFARvq0ekDNtvj3Vqg);
NTI_Q79bEtFARvq0ekDNtvj3Vqg_.kind = 14;
NTI_Q79bEtFARvq0ekDNtvj3Vqg_.base = 0;
NTI_Q79bEtFARvq0ekDNtvj3Vqg_.flags = 3;
for (TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4 = 0; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4 < 4; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4++) {
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4+0].kind = 1;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4+0].offset = TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4+0].name = TM_f9bP3LqjpgpB9cXL8Nnak7tQ_3[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4];
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_2[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4] = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_4+0];
}
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[0].offset = 0;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[1].offset = 1;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[2].offset = 2;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[3].offset = 23;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[4].len = 4; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[4].kind = 2; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[4].sons = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_2[0];
NTI_Q79bEtFARvq0ekDNtvj3Vqg_.node = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[4];
NTI_Q79bEtFARvq0ekDNtvj3Vqg_.flags = 1<<2;
NTI_NQT1bItGG2X9byGdrWX7ujw_.size = sizeof(tyEnum_SockType_NQT1bItGG2X9byGdrWX7ujw);
NTI_NQT1bItGG2X9byGdrWX7ujw_.kind = 14;
NTI_NQT1bItGG2X9byGdrWX7ujw_.base = 0;
NTI_NQT1bItGG2X9byGdrWX7ujw_.flags = 3;
for (TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7 = 0; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7 < 4; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7++) {
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7+5].kind = 1;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7+5].offset = TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7+5].name = TM_f9bP3LqjpgpB9cXL8Nnak7tQ_6[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7];
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_5[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7] = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_7+5];
}
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[5].offset = 1;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[6].offset = 2;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[7].offset = 3;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[8].offset = 5;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[9].len = 4; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[9].kind = 2; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[9].sons = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_5[0];
NTI_NQT1bItGG2X9byGdrWX7ujw_.node = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[9];
NTI_NQT1bItGG2X9byGdrWX7ujw_.flags = 1<<2;
NTI_dqJ1OqRGclxIMMdSLRzzXg_.size = sizeof(tyEnum_Protocol_dqJ1OqRGclxIMMdSLRzzXg);
NTI_dqJ1OqRGclxIMMdSLRzzXg_.kind = 14;
NTI_dqJ1OqRGclxIMMdSLRzzXg_.base = 0;
NTI_dqJ1OqRGclxIMMdSLRzzXg_.flags = 3;
for (TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10 = 0; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10 < 6; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10++) {
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10+10].kind = 1;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10+10].offset = TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10+10].name = TM_f9bP3LqjpgpB9cXL8Nnak7tQ_9[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10];
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_8[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10] = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[TM_f9bP3LqjpgpB9cXL8Nnak7tQ_10+10];
}
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[10].offset = 6;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[11].offset = 17;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[12].offset = 18;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[13].offset = 19;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[14].offset = 20;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[15].offset = 21;
TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[16].len = 6; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[16].kind = 2; TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[16].sons = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_8[0];
NTI_dqJ1OqRGclxIMMdSLRzzXg_.node = &TM_f9bP3LqjpgpB9cXL8Nnak7tQ_0[16];
NTI_dqJ1OqRGclxIMMdSLRzzXg_.flags = 1<<2;
}

