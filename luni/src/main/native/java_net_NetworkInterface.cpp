
#include "JNIHelp.h"
#include "JniConstants.h"
#include "JniException.h"
#include "ScopedPrimitiveArray.h"
#include "ScopedUtfChars.h"
#include "toStringArray.h"

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <net/if.h>
#include <sys/ioctl.h>
#include <netinet/in.h>
#include <ifaddrs.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>

#include <net/if_dl.h>
#include <sys/sockio.h>

static jboolean ioctl_ifreq(JNIEnv* env, jstring interfaceName, struct ifreq* ifreq, int request) {
    ScopedUtfChars name(env, interfaceName);
  
    if (name.c_str() == NULL) {
      return JNI_FALSE;
    }

    int sock = socket(AF_INET, SOCK_DGRAM, 0);
  
    if (sock < 0) {
        jniThrowSocketException(env, errno);
      
        return JNI_FALSE;
    }

    memset(ifreq, 0, sizeof(struct ifreq));
  
    strcpy(ifreq->ifr_name, name.c_str());
  
#ifndef MOE
    if (ioctl(sock, request, ifreq) < 0) {
#else
    if (ioctl(sock, (unsigned int)request, ifreq) < 0) {
#endif
        close(sock);
        jniThrowSocketException(env, errno);
      
        return JNI_FALSE;
    }
  
    close(sock);

    return JNI_TRUE;

}

static jint getifaddrs_flags(JNIEnv* env, jstring interfaceName) {
    //MOE: ioctl doesn't work on ios 64 bit (EOPNOTSUPP), use getifaddr instead
    struct ifaddrs *if_addrs = NULL;
    struct ifaddrs *if_addr = NULL;
    ScopedUtfChars name(env, interfaceName);
    
    if (0 == getifaddrs(&if_addrs)) {
        for (if_addr = if_addrs; if_addr != NULL; if_addr = if_addr->ifa_next) {
            if (if_addr->ifa_addr->sa_family == AF_INET && !strcmp(if_addr->ifa_name, name.c_str())){
                return (jint)(if_addr->ifa_flags);
            }
        }
    }
    
    freeifaddrs(if_addrs);
    if_addrs = NULL;
    
    return 0;
}


static jboolean iterateAddrInfo(JNIEnv* env, jstring interfaceName, jboolean (*callback)(JNIEnv*, struct ifaddrs *, void*), void* data) {
    ScopedUtfChars name(env, interfaceName);
    
    if (name.c_str() == NULL)
    {
      return JNI_FALSE;
    }
  
    struct ifaddrs *ap;
    struct ifaddrs *apiter;
  
    if (getifaddrs(&ap) < 0) {
        jniThrowSocketException(env, errno);
      
        return JNI_FALSE;
    }

    for (apiter = ap; apiter != NULL; apiter = apiter->ifa_next) {
        if (!strcmp(apiter->ifa_name, name.c_str())) {
            if (callback(env, apiter, data) == JNI_FALSE) {
                break;
            }
        }
    }

    freeifaddrs(ap);
  
    return JNI_TRUE;
}

extern "C" jobjectArray Java_java_net_NetworkInterface_getInterfaceNames(JNIEnv* env, jclass) {

    struct if_nameindex* ifs = if_nameindex();
  
    if (!ifs) {
        jniThrowOutOfMemoryError(env, "");
        return NULL;
    }
  
    int addressCount = 0;
  
    while (ifs[addressCount].if_index > 0) {
        addressCount++;
    }

    // Prepare output array.
    jobjectArray result = env->NewObjectArray(addressCount, JniConstants::stringClass, NULL);
  
    if (result != NULL)
    {
        for (int i = 0; i < addressCount; i++)
        {
            ScopedLocalRef<jstring> name(env, env->NewStringUTF(ifs[i].if_name));
          
            if (env->ExceptionCheck())
            {
              break;
            }
          
            env->SetObjectArrayElement(result, i, name.get());
          
            if (env->ExceptionCheck())
            {
              break;
            }
        }
    }

    if_freenameindex(ifs);
  
    return result;
}

extern "C" jint Java_java_net_NetworkInterface_getInterfaceIndex(JNIEnv* env, jclass, jstring interfaceName) {
    ScopedUtfChars name(env, interfaceName);
    
    if (name.c_str() == NULL) {
      return -1;
    }
  
    return if_nametoindex(name.c_str());
}

extern "C" jint Java_java_net_NetworkInterface_getInterfaceFlags(JNIEnv* env, jclass, jstring interfaceName) {
    struct ifreq ifreq;
  
    /*if (!ioctl_ifreq(env, interfaceName, &ifreq, SIOCGIFFLAGS)) {
        return 0;
    }*/
    jint flags = getifaddrs_flags(env, interfaceName);
  
    return (flags & 0xffff);
}

extern "C" jint Java_java_net_NetworkInterface_getMTU(JNIEnv* env, jclass, jstring interfaceName) {
    struct ifreq ifreq;
  
    if (!ioctl_ifreq(env, interfaceName, &ifreq, SIOCGIFMTU)) {
        return 0;
    }
  
    return ifreq.ifr_mtu;
}

static jboolean getHardwareAddressIterator(JNIEnv* env, struct ifaddrs *ia, void* data) {
    jbyteArray* resultPtr = (jbyteArray*) data;
  
    int addrLen = 6;
  
    if (ia->ifa_addr->sa_family == AF_LINK)
    {
        struct sockaddr_dl* addr = (struct sockaddr_dl*) ia->ifa_addr;
      
        if (addr->sdl_alen == addrLen)
        {
            char* buffer = (char*) LLADDR(addr);
          
            *resultPtr = env->NewByteArray(addrLen);
          
            if (*resultPtr != NULL)
            {
                ScopedByteArrayRW bytes(env, *resultPtr);
              
                memcpy(reinterpret_cast<void*>(bytes.get()), buffer, addrLen);
            }
          
            return JNI_FALSE;
        }
    }
  
    return JNI_TRUE;
}

extern "C" jbyteArray Java_java_net_NetworkInterface_getHardwareAddress(JNIEnv* env, jclass, jstring interfaceName) {
    jbyteArray result = NULL;

    iterateAddrInfo(env, interfaceName, getHardwareAddressIterator, &result);

    return result;
}

static jboolean countIpv6AddressesIterator(JNIEnv* env, struct ifaddrs *ia, void* data) {
    jint* count = (jint*) data;
  
    if (ia->ifa_addr && ia->ifa_addr->sa_family == AF_INET6) {
        (*count)++;
    }
  
    return JNI_TRUE;
}

typedef struct {
    jbyteArray result;
    jint index;
} IPV6_ADDR_DATA;

extern "C" jboolean getIpv6AddressesIterator(JNIEnv* env, struct ifaddrs *ia, void* _data) {
    IPV6_ADDR_DATA* data = (IPV6_ADDR_DATA*) _data;
  
    int addrLen = 16;
  
    if (ia->ifa_addr && ia->ifa_addr->sa_family == AF_INET6)
    {
        struct sockaddr_in6* addr = (struct sockaddr_in6*) ia->ifa_addr;
        struct sockaddr_in6* netmask = (struct sockaddr_in6*) ia->ifa_netmask;
      
        ScopedByteArrayRW bytes(env, data->result);
      
        unsigned char* buffer = reinterpret_cast<unsigned char*>(bytes.get());
      
        memcpy(buffer + (addrLen * 2 * data->index), addr->sin6_addr.s6_addr, addrLen);
      
        if (netmask)
        {
            memcpy(buffer + (addrLen * 2 * data->index) + addrLen, netmask->sin6_addr.s6_addr, addrLen);
        }
      
        data->index++;
    }
  
    return JNI_TRUE;
}

extern "C" jbyteArray Java_java_net_NetworkInterface_getIpv6Addresses(JNIEnv* env, jclass, jstring interfaceName) {
    int count = 0;
  
    if (iterateAddrInfo(env, interfaceName, countIpv6AddressesIterator, &count) == JNI_FALSE) {
        return NULL;
    }

    if (count == 0) {
        return NULL;
    }

    jbyteArray result = env->NewByteArray(16 * 2 * count);
  
    if (result == NULL) {
        return NULL;
    }

    IPV6_ADDR_DATA data = {result, 0};
  
    if (iterateAddrInfo(env, interfaceName, getIpv6AddressesIterator, &data) == JNI_FALSE) {
        return NULL;
    }
  
    return result;
}

void register_java_net_NetworkInterface(JNIEnv* env) {
}
