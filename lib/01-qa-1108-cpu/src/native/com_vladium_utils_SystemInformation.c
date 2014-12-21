/* ------------------------------------------------------------------------- */
/*
 * An implementation of JNI methods in com.vladium.utils.SystemInformation
 * class. The author compiled it using Microsoft Visual C++ but the code
 * should be easy to use with any compiler for win32 platform.
 *
 * For simplicity, this implementaion assumes JNI 1.2+ and omits error handling.
 *
 * (C) 2002, Vladimir Roubtsov [vlad@trilogy.com]
 */
/* ------------------------------------------------------------------------- */

#include <windows.h>
#include <process.h>
#include <winbase.h>

#include "com_vladium_utils_SystemInformation.h"

static jint s_PID;
static HANDLE s_currentProcess;
static int s_numberOfProcessors;

/* ------------------------------------------------------------------------- */

/*
 * A helper function for converting FILETIME to a LONGLONG [safe from memory
 * alignment point of view].
 */
static LONGLONG
fileTimeToInt64 (const FILETIME * time)
{
    ULARGE_INTEGER _time;

    _time.LowPart = time->dwLowDateTime;
    _time.HighPart = time->dwHighDateTime;

    return _time.QuadPart;
}
/* ......................................................................... */

/*
 * This method was added in JNI 1.2. It is executed once before any other
 * methods are called and is ostensibly for negotiating JNI spec versions, but
 * can also be conveniently used for initializing variables that will not
 * change throughout the lifetime of this process.
 */
JNIEXPORT jint JNICALL
JNI_OnLoad (JavaVM * vm, void * reserved)
{
    SYSTEM_INFO systemInfo;
        
    s_PID = _getpid ();
    s_currentProcess = GetCurrentProcess ();

    GetSystemInfo (& systemInfo);
    s_numberOfProcessors = systemInfo.dwNumberOfProcessors;

    return JNI_VERSION_1_2;
}
/* ......................................................................... */

/*
 * Class:     com_vladium_utils_SystemInformation
 * Method:    getProcessID
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_com_vladium_utils_SystemInformation_getProcessID (JNIEnv * env, jclass cls)
{
    return s_PID;
}
/* ......................................................................... */

/*
 * Class:     com_vladium_utils_SystemInformation
 * Method:    getProcessCPUTime
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_com_vladium_utils_SystemInformation_getProcessCPUTime (JNIEnv * env, jclass cls)
{
    FILETIME creationTime, exitTime, kernelTime, userTime;
    
    GetProcessTimes (s_currentProcess, & creationTime, & exitTime, & kernelTime, & userTime);

    return (jlong) ((fileTimeToInt64 (& kernelTime) + fileTimeToInt64 (& userTime)) /
        (s_numberOfProcessors * 10000));
}
/* ......................................................................... */

/* define min elapsed time (in units of 10E-7 sec): */
#define MIN_ELAPSED_TIME (10000)

/*
 * Class:     com_vladium_utils_SystemInformation
 * Method:    getProcessCPUUsage
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_com_vladium_utils_SystemInformation_getProcessCPUUsage (JNIEnv * env, jclass cls)
{
    FILETIME creationTime, exitTime, kernelTime, userTime, nowTime;   
    LONGLONG elapsedTime;
    
    GetProcessTimes (s_currentProcess, & creationTime, & exitTime, & kernelTime, & userTime);
    GetSystemTimeAsFileTime (& nowTime);

    /*
        NOTE: win32 system time is not very precise [~10ms resolution], use
        sufficiently long sampling intervals if you make use of this method.
    */
    
    elapsedTime = fileTimeToInt64 (& nowTime) - fileTimeToInt64 (& creationTime);
    
    if (elapsedTime < MIN_ELAPSED_TIME)
        return 0.0;
    else  
        return ((jdouble) (fileTimeToInt64 (& kernelTime) + fileTimeToInt64 (& userTime))) /
            (s_numberOfProcessors * elapsedTime);
}

#undef MIN_ELAPSED_TIME

/* ------------------------------------------------------------------------- */
/* end of file */
