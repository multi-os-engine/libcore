/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "JNIHelp.h"
#include "jni.h"
#include "errno.h"

#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/socket.h>
#include <net/if.h>
#include <netinet/in.h>
#include <sys/ioctl.h>
#include <linux/netlink.h>
#include <linux/rtnetlink.h>
#include <arpa/inet.h>

#define LOG_TAG "NetworkInterface"

//--------------------------------------------------------------------
// TODO copied from OSNetworkSystem. Might get into a separate .h file
/**
 * Throws an IOException with the given message.
 */
static void throwSocketException(JNIEnv *env, const char *message) {
    jclass exClass = (*env)->FindClass(env, "java/net/SocketException");

    if(exClass == NULL) {
        LOGE("Unable to find class java/net/SocketException");
    } else {
        (*env)->ThrowNew(env, exClass, message);
    }
}


/**
 * Throws a NullPointerException.
 */
static void throwNullPointerException(JNIEnv *env) {
    jclass exClass = (*env)->FindClass(env, "java/lang/NullPointerException");

    if(exClass == NULL) {
        LOGE("Unable to find class java/lang/NullPointerException");
    } else {
        (*env)->ThrowNew(env, exClass, NULL);
    }
}

/**
 * @name Socket Errors
 * Error codes for socket operations
 *
 * @internal SOCKERR* range from -200 to -299 avoid overlap
 */
#define SOCKERR_BADSOCKET          -200 /* generic error */
#define SOCKERR_NOTINITIALIZED     -201 /* socket library uninitialized */
#define SOCKERR_BADAF              -202 /* bad address family */
#define SOCKERR_BADPROTO           -203 /* bad protocol */
#define SOCKERR_BADTYPE            -204 /* bad type */
#define SOCKERR_SYSTEMBUSY         -205 /* system busy handling requests */
#define SOCKERR_SYSTEMFULL         -206 /* too many sockets */
#define SOCKERR_NOTCONNECTED       -207 /* socket is not connected */
#define SOCKERR_INTERRUPTED        -208 /* the call was cancelled */
#define SOCKERR_TIMEOUT            -209 /* the operation timed out */
#define SOCKERR_CONNRESET          -210 /* the connection was reset */
#define SOCKERR_WOULDBLOCK         -211 /* the socket is marked as nonblocking operation would block */
#define SOCKERR_ADDRNOTAVAIL       -212 /* address not available */
#define SOCKERR_ADDRINUSE          -213 /* address already in use */
#define SOCKERR_NOTBOUND           -214 /* the socket is not bound */
#define SOCKERR_UNKNOWNSOCKET      -215 /* resolution of fileDescriptor to socket failed */
#define SOCKERR_INVALIDTIMEOUT     -216 /* the specified timeout is invalid */
#define SOCKERR_FDSETFULL          -217 /* Unable to create an FDSET */
#define SOCKERR_TIMEVALFULL        -218 /* Unable to create a TIMEVAL */
#define SOCKERR_REMSOCKSHUTDOWN    -219 /* The remote socket has shutdown gracefully */
#define SOCKERR_NOTLISTENING       -220 /* listen() was not invoked prior to accept() */
#define SOCKERR_NOTSTREAMSOCK      -221 /* The socket does not support connection-oriented service */
#define SOCKERR_ALREADYBOUND       -222 /* The socket is already bound to an address */
#define SOCKERR_NBWITHLINGER       -223 /* The socket is marked non-blocking & SO_LINGER is non-zero */
#define SOCKERR_ISCONNECTED        -224 /* The socket is already connected */
#define SOCKERR_NOBUFFERS          -225 /* No buffer space is available */
#define SOCKERR_HOSTNOTFOUND       -226 /* Authoritative Answer Host not found */
#define SOCKERR_NODATA             -227 /* Valid name, no data record of requested type */
#define SOCKERR_BOUNDORCONN        -228 /* The socket has not been bound or is already connected */
#define SOCKERR_OPNOTSUPP          -229 /* The socket does not support the operation */
#define SOCKERR_OPTUNSUPP          -230 /* The socket option is not supported */
#define SOCKERR_OPTARGSINVALID     -231 /* The socket option arguments are invalid */
#define SOCKERR_SOCKLEVELINVALID   -232 /* The socket level is invalid */
#define SOCKERR_TIMEOUTFAILURE     -233
#define SOCKERR_SOCKADDRALLOCFAIL  -234 /* Unable to allocate the sockaddr structure */
#define SOCKERR_FDSET_SIZEBAD      -235 /* The calculated maximum size of the file descriptor set is bad */
#define SOCKERR_UNKNOWNFLAG        -236 /* The flag is unknown */
#define SOCKERR_MSGSIZE            -237 /* The datagram was too big to fit the specified buffer & was truncated. */
#define SOCKERR_NORECOVERY         -238 /* The operation failed with no recovery possible */
#define SOCKERR_ARGSINVALID        -239 /* The arguments are invalid */
#define SOCKERR_BADDESC            -240 /* The socket argument is not a valid file descriptor */
#define SOCKERR_NOTSOCK            -241 /* The socket argument is not a socket */
#define SOCKERR_HOSTENTALLOCFAIL   -242 /* Unable to allocate the hostent structure */
#define SOCKERR_TIMEVALALLOCFAIL   -243 /* Unable to allocate the timeval structure */
#define SOCKERR_LINGERALLOCFAIL    -244 /* Unable to allocate the linger structure */
#define SOCKERR_IPMREQALLOCFAIL    -245 /* Unable to allocate the ipmreq structure */
#define SOCKERR_FDSETALLOCFAIL     -246 /* Unable to allocate the fdset structure */
#define SOCKERR_OPFAILED           -247
#define SOCKERR_VALUE_NULL         -248 /* The value indexed was NULL */
#define SOCKERR_CONNECTION_REFUSED -249 /* connection was refused */
#define SOCKERR_ENETUNREACH        -250 /* network is not reachable */
#define SOCKERR_EACCES             -251 /* permissions do not allow action on socket */

/**
 * Answer the errorString corresponding to the errorNumber, if available.
 * This function will answer a default error string, if the errorNumber is not
 * recognized.
 *
 * This function will have to be reworked to handle internationalization properly, removing
 * the explicit strings.
 *
 * @param anErrorNum    the error code to resolve to a human readable string
 *
 * @return  a human readable error string
 */

static char * netLookupErrorString(int anErrorNum) {
    switch(anErrorNum) {
        case SOCKERR_BADSOCKET:
            return "Bad socket";
        case SOCKERR_NOTINITIALIZED:
            return "Socket library uninitialized";
        case SOCKERR_BADAF:
            return "Bad address family";
        case SOCKERR_BADPROTO:
            return "Bad protocol";
        case SOCKERR_BADTYPE:
            return "Bad type";
        case SOCKERR_SYSTEMBUSY:
            return "System busy handling requests";
        case SOCKERR_SYSTEMFULL:
            return "Too many sockets allocated";
        case SOCKERR_NOTCONNECTED:
            return "Socket is not connected";
        case SOCKERR_INTERRUPTED:
            return "The call was cancelled";
        case SOCKERR_TIMEOUT:
            return "The operation timed out";
        case SOCKERR_CONNRESET:
            return "The connection was reset";
        case SOCKERR_WOULDBLOCK:
            return "The socket is marked as nonblocking operation would block";
        case SOCKERR_ADDRNOTAVAIL:
            return "The address is not available";
        case SOCKERR_ADDRINUSE:
            return "The address is already in use";
        case SOCKERR_NOTBOUND:
            return "The socket is not bound";
        case SOCKERR_UNKNOWNSOCKET:
            return "Resolution of the FileDescriptor to socket failed";
        case SOCKERR_INVALIDTIMEOUT:
            return "The specified timeout is invalid";
        case SOCKERR_FDSETFULL:
            return "Unable to create an FDSET";
        case SOCKERR_TIMEVALFULL:
            return "Unable to create a TIMEVAL";
        case SOCKERR_REMSOCKSHUTDOWN:
            return "The remote socket has shutdown gracefully";
        case SOCKERR_NOTLISTENING:
            return "Listen() was not invoked prior to accept()";
        case SOCKERR_NOTSTREAMSOCK:
            return "The socket does not support connection-oriented service";
        case SOCKERR_ALREADYBOUND:
            return "The socket is already bound to an address";
        case SOCKERR_NBWITHLINGER:
            return "The socket is marked non-blocking & SO_LINGER is non-zero";
        case SOCKERR_ISCONNECTED:
            return "The socket is already connected";
        case SOCKERR_NOBUFFERS:
            return "No buffer space is available";
        case SOCKERR_HOSTNOTFOUND:
            return "Authoritative Answer Host not found";
        case SOCKERR_NODATA:
            return "Valid name, no data record of requested type";
        case SOCKERR_BOUNDORCONN:
            return "The socket has not been bound or is already connected";
        case SOCKERR_OPNOTSUPP:
            return "The socket does not support the operation";
        case SOCKERR_OPTUNSUPP:
            return "The socket option is not supported";
        case SOCKERR_OPTARGSINVALID:
            return "The socket option arguments are invalid";
        case SOCKERR_SOCKLEVELINVALID:
            return "The socket level is invalid";
        case SOCKERR_TIMEOUTFAILURE:
            return "The timeout operation failed";
        case SOCKERR_SOCKADDRALLOCFAIL:
            return "Failed to allocate address structure";
        case SOCKERR_FDSET_SIZEBAD:
            return "The calculated maximum size of the file descriptor set is bad";
        case SOCKERR_UNKNOWNFLAG:
            return "The flag is unknown";
        case SOCKERR_MSGSIZE:
            return "The datagram was too big to fit the specified buffer, so truncated";
        case SOCKERR_NORECOVERY:
            return "The operation failed with no recovery possible";
        case SOCKERR_ARGSINVALID:
            return "The arguments are invalid";
        case SOCKERR_BADDESC:
            return "The socket argument is not a valid file descriptor";
        case SOCKERR_NOTSOCK:
            return "The socket argument is not a socket";
        case SOCKERR_HOSTENTALLOCFAIL:
            return "Unable to allocate the hostent structure";
        case SOCKERR_TIMEVALALLOCFAIL:
            return "Unable to allocate the timeval structure";
        case SOCKERR_LINGERALLOCFAIL:
            return "Unable to allocate the linger structure";
        case SOCKERR_IPMREQALLOCFAIL:
            return "Unable to allocate the ipmreq structure";
        case SOCKERR_FDSETALLOCFAIL:
            return "Unable to allocate the fdset structure";
        case SOCKERR_CONNECTION_REFUSED:
            return "Connection refused";

        default:
            return "unkown error";
    }
}

/**
 * Converts a native address structure to an array. Throws a
 * NullPointerException or an IOException in case of error. This is
 * signaled by a return value of -1. The normal return value is 0.
 */
static int structInToJavaAddress(
        JNIEnv *env, void *address, jbyteArray java_address, int length) {

    if(java_address == NULL) {
        throwNullPointerException(env);
        return -1;
    }

    if((*env)->GetArrayLength(env, java_address) != length) {
        jniThrowIOException(env, errno);
        return -1;
    }

    jbyte *java_address_bytes;

    java_address_bytes = (*env)->GetByteArrayElements(env, java_address, NULL);

    memcpy(java_address_bytes, address, length);

    (*env)->ReleaseByteArrayElements(env, java_address, java_address_bytes, 0);

    return 0;
}

static jobject structInToInetAddress(JNIEnv *env, void *address, int fam) {
    jbyteArray bytes;
    int success;
    int length = 0;
    char addrchar[200];

    LOGI("InToInetAddress: %s", inet_ntop(fam, address, addrchar, sizeof(addrchar)));

    if (fam == AF_INET) length = sizeof(struct in_addr); else 
                      if (fam == AF_INET6) length = sizeof(struct in6_addr);
    bytes = (*env)->NewByteArray(env, length);

    if(bytes == NULL) {
        return NULL;
    }

    success = structInToJavaAddress(env, address, bytes, length);

    if(success < 0) {
        return NULL;
    }

    jclass iaddrclass = (*env)->FindClass(env, "java/net/InetAddress");

    if(iaddrclass == NULL) {
        LOGE("Can't find java/net/InetAddress");
        jniThrowException(env, "java/lang/ClassNotFoundException", "java.net.InetAddress");
        return NULL;
    }

    jmethodID iaddrgetbyaddress = (*env)->GetStaticMethodID(env, iaddrclass, "getByAddress", "([B)Ljava/net/InetAddress;");

    if(iaddrgetbyaddress == NULL) {
        LOGE("Can't find method InetAddress.getByAddress(byte[] val)");
        jniThrowException(env, "java/lang/NoSuchMethodError", "InetAddress.getByAddress(byte[] val)");
        return NULL;
    }

    return (*env)->CallStaticObjectMethod(env, iaddrclass, iaddrgetbyaddress, bytes);
}
//--------------------------------------------------------------------






















/* structure for returning either and IPV4 or IPV6 ip address */
typedef struct ipAddress_struct {
    union {
        char bytes[sizeof(struct in_addr)];
        struct in_addr inAddr;
        char bytes6[sizeof(struct in6_addr)];
        struct in6_addr inAddr6;
    } addr;
    unsigned int  family;
    unsigned int  scope;
    struct ipAddress_struct *nextaddress;
} ipAddress_struct;

/* structure for returning network interface information */
typedef struct NetworkInterface_struct {
    char *name;
    char *displayName;
    unsigned int  numberAddresses;
    unsigned int  index;
    unsigned int  flags;
    struct ipAddress_struct *addresses;
    struct NetworkInterface_struct *nextinterface;
} NetworkInterface_struct;



static struct NetworkInterface_struct *network_interfaces = (struct NetworkInterface_struct *)NULL;
static struct sockaddr_nl addr_nl;


void free_network_interface() {
    struct ipAddress_struct *addr, *atmp;
    struct NetworkInterface_struct *itmp;
    while (network_interfaces){
        /* free addresses */
        addr = network_interfaces->addresses;
        while (addr != NULL){
            atmp = addr;
            addr = addr->nextaddress;
            free(atmp);
        }

        if (network_interfaces->name != NULL) {
            free(network_interfaces->name);
        }

        if (network_interfaces->displayName != NULL) {
            free(network_interfaces->displayName);
        }
        itmp = network_interfaces;
        network_interfaces = network_interfaces->nextinterface;
        free(itmp);
    }
}

char *salloccopy(char *s) {
   char *rs = NULL;
   if (s != NULL) {
       rs = malloc(strlen(s)+1);
       if (rs != NULL) {
          strncpy(rs, s, strlen(s));
          rs[strlen(s)] = '\0';
       }
    } 
    return rs;
}
      

int insert_interface_address(unsigned int if_index, char *if_name, 
                             void *if_addr, int addr_type, unsigned int flags) {
    char *name, *dname;
    struct ipAddress_struct *addr = NULL;
    struct NetworkInterface_struct *interface, *primary_interface, *last;
    struct in_addr *inp;
    struct in_addr6 *inp6;
    char addrchar[200];
    unsigned int fl;
  

    LOGI ("%d: interface: %s %s", 
         if_index, if_name, inet_ntop(addr_type, if_addr, addrchar, sizeof(addrchar)));
 
    /* allocate interface and address entry */
     
    dname = salloccopy(if_name);
    if ((dname == NULL) && (if_name != NULL)) {
        LOGE("Insuffient memory");
        return -1;
    }
    
    if (if_addr != NULL) {
        addr = malloc(sizeof(ipAddress_struct));
        if (addr == NULL) {
            free(dname);
            return -1;
        }
        /* insert addr fields */
 
        addr->nextaddress = NULL;
        addr->family = addr_type;
        if (addr_type == AF_INET) {
            addr->addr.inAddr = *((struct in_addr *)if_addr);
        } else if (addr_type == AF_INET6) {
            addr->addr.inAddr6 = *((struct in6_addr *)if_addr);
        }
    }

    /* search for interface index */     
    interface = network_interfaces;
    fl = flags;
    primary_interface = NULL;
    last = NULL;
    while (interface != NULL) {
        last = interface;
        if (interface->index == if_index) {
           if (if_name == NULL) break;
           if (strcmp(interface->name, if_name) == 0) {
               /* found matching index and interface name */
               break;
           } else {
               /* found interface with matching index, but different if_name */
               /* insert new interface */
               primary_interface = interface;
           }
       }
       interface = interface->nextinterface;
    }

    if (interface == NULL) {  
        /* create new interface struct */  
        interface = malloc(sizeof(struct NetworkInterface_struct));
        if (interface == NULL) {
            free(addr);
            free(dname);
            return -1;
        }

        if (primary_interface != NULL) {
            /* copy interfacename and flags from primary */
            name = primary_interface->name;
            fl = primary_interface->flags;
        } else {
            /* interface name = displayname */
            name = dname;
        }
        interface->name = salloccopy(name);
        if (interface->name == NULL) {
            free(interface);
            free(addr);
            free(dname);
            LOGE("Insufficient memory");
            return -1;
        }
        interface->displayName = dname;
        interface->index = if_index;
        interface->flags = fl;
        interface->addresses = NULL;
        interface->nextinterface = NULL;
        if (last == NULL) {
            network_interfaces = interface;
        } else {
            last->nextinterface = interface;
        }
    }
    if (addr != NULL) {
        addr-> nextaddress = interface->addresses;
        interface->addresses = addr;
    }
    return 0;
}

void print_interfaces() {
    struct ipAddress_struct *addr;
    struct NetworkInterface_struct *interface;
    char addrchar[200];

    interface = network_interfaces;
    while (interface != NULL) {
        LOGI("%d: %s flags:%x",
                interface->index, interface->displayName, interface->flags);
        addr = interface->addresses;
        while (addr != NULL) {
            LOGI("%d: %s %s",
                interface->index, interface->displayName,
                inet_ntop(addr->family, &addr->addr.inAddr, addrchar, sizeof(addrchar)));
            addr = addr->nextaddress;
         }
         interface = interface->nextinterface;
      }
}

typedef enum { rtm_link, rtm_addr, rtm_rt } rtm_head_type;
typedef struct rtm_header_universal {
    union {
        struct ifinfomsg ifinfo;
        struct ifaddrmsg ifaddr;
        struct rtmsg rt;
    } rtm;
    rtm_head_type rtmtype;
} rtm_header_universal;

void rtm_head_init(rtm_header_universal *header, int rtm_type) {
    
    memset(header, 0, sizeof(rtm_header_universal));
    header->rtmtype = rtm_type;
}

int rtm_head_len(rtm_header_universal *header) {
    switch (header->rtmtype) {
    case rtm_link: return sizeof(struct ifinfomsg);
    case rtm_addr: return sizeof(struct ifaddrmsg);
    case rtm_rt: return sizeof(struct rtmsg);
    default: return 0;
    }
}


int interface_from_rtm_newlink(struct nlmsghdr *nlmp) {

    struct ifinfomsg *iftmp;
    struct rtattr *rtatp;
    int rtattrlen;
    char *if_name;
    char *oper;

    iftmp = (struct ifinfomsg *)NLMSG_DATA(nlmp);
    rtatp = (struct rtattr *)IFLA_RTA(iftmp);

        /* Start displaying the index of the interface */

    if_name = NULL;
 
    if (iftmp->ifi_flags & IFF_UP) oper = "Up"; else oper = "Down";
    LOGD("Index: %d, Type: %d flags: %x operational: %s", 
            iftmp->ifi_index, iftmp->ifi_type, iftmp->ifi_flags, oper );

    rtattrlen = IFLA_PAYLOAD(nlmp);

    for (; RTA_OK(rtatp, rtattrlen); rtatp = RTA_NEXT(rtatp, rtattrlen)) {              

        /* Routing attributes                                                 *
         *    rta_type             value type         description             *
         *    --------------------------------------------------------------  *
         *    IFLA_UNSPEC          -                  unspecified.            *
         *    IFLA_ADDRESS         hardware address   interface L2 address    *
         *    IFLA_BROADCAST       hardware address   L2 broadcast address.   *
         *    IFLA_IFNAME          asciiz string      Device name.            *
 
         *    IFLA_MTU             unsigned int       MTU of the device.      *
         *    IFLA_LINK            int                Link type.              *
         *    IFLA_QDISC           asciiz string      Queueing discipline.    *
         *    IFLA_STATS           see below          Interface Statistics.   *

         *    The value type for IFLA_STATS is struct net_device_stats.       */



        if (rtatp->rta_type == IFLA_IFNAME){
            if_name = RTA_DATA(rtatp);
            LOGD("ifname: %s", if_name);
        }
    }
    return insert_interface_address(iftmp->ifi_index, if_name, NULL, 0, iftmp->ifi_flags);

}


int interface_addresses_from_rtm_newaddr(struct nlmsghdr *nlmp) {

    struct ifaddrmsg *rtmp;
    struct rtattr *rtatp;
    int rtattrlen;
    struct ifa_cacheinfo *cache_info;
    struct in6_addr *if_addr = NULL;
    char *if_name;
    char *fam;
    char addrchar[200];


    rtmp = (struct ifaddrmsg *)NLMSG_DATA(nlmp);
    rtatp = (struct rtattr *)IFA_RTA(rtmp);

        /* Start displaying the index of the interface */

    if_name = NULL;
              
    if (rtmp->ifa_family == AF_INET6) {
        fam = "AF_INET6";
    } else if (rtmp->ifa_family == AF_INET) {
        fam = "AF_INET";
    } else {
        fam = "AF prtotocol unknown";
    }
    LOGD("Index: %d Prefix: %d Family: %s", 
                     rtmp->ifa_index, rtmp->ifa_prefixlen, fam);

    rtattrlen = IFA_PAYLOAD(nlmp);

    for (; RTA_OK(rtatp, rtattrlen); rtatp = RTA_NEXT(rtatp, rtattrlen)) {
     
        /* The table below is taken from man pages.                           *
         * Attributes                                                         *
         * rta_type        value type             description                 *
         * -------------------------------------------------------------      *
         * IFA_UNSPEC      -                      unspecified.                *
         * IFA_ADDRESS     raw protocol address   interface address           *
         * IFA_LOCAL       raw protocol address   local address               *
         * IFA_LABEL       asciiz string          name of the interface       *
         * IFA_BROADCAST   raw protocol address   broadcast address.          *
         * IFA_ANYCAST     raw protocol address   anycast address             *
         * IFA_CACHEINFO   struct ifa_cacheinfo   Address information.        */


        if (rtatp->rta_type == IFA_ADDRESS){
            if_addr = RTA_DATA(rtatp);
        }

        if (rtatp->rta_type == IFA_LABEL){
            if_name = RTA_DATA(rtatp);
            LOGD("label: %s", if_name);
        }
    }
    return insert_interface_address(rtmp->ifa_index, if_name, if_addr, rtmp->ifa_family, 0);

}

int get_interface_addresses(int netlink_socket, int netlink_oper, rtm_header_universal *head)

{
      struct {
              struct nlmsghdr n;
              unsigned char buf[1024];                
      } req;

      struct rtattr *rta;
      int status, len;
      char buf[16384];
      struct nlmsghdr *nlmp;
      struct nlmsgerr *nlerr;

      memset(&req, 0, sizeof(req));
      len = rtm_head_len(head);
      req.n.nlmsg_len = NLMSG_LENGTH(len);
      req.n.nlmsg_flags = NLM_F_REQUEST | NLM_F_ROOT;
      req.n.nlmsg_type = netlink_oper;
      /* copy header */
      memcpy(req.buf, (char *) &head->rtm, len);

      /* Time to send and recv the message from kernel */

      status = send(netlink_socket, &req, req.n.nlmsg_len, 0);

      if (status < 0) {
              perror("send");
              return 1;
      }

      for (;;) {
        
        status = recv(netlink_socket, buf, sizeof(buf), 0);

        LOGI("netlink message received Length: %d", status);

      	if (status < 0) {
              perror("recv");
              return 1;
      	}

      	if(status == 0){
              return 1;
      	}

      /* Typically the message is stored in buf, so we need to parse the message to *
        * get the required data for our display. */

      	for(nlmp = (struct nlmsghdr *)buf; status > (int) sizeof(*nlmp);){
              int len = nlmp->nlmsg_len;
              int req_len = len - sizeof(*nlmp);
              int res = 0;

              LOGD("nlmsg_type: %d nlmsg_lgt: %d", nlmp->nlmsg_type, len);

              if (req_len<0 || len>status) {
                      LOGE("error");
                      return -1;
              }

              if (!NLMSG_OK(nlmp, (unsigned int) status)) {
                      LOGE("NLMSG not OK");
                      return 1;
              }

              switch (nlmp->nlmsg_type) {
              case NLMSG_DONE: {
                       return 0;
                  }
              case NLMSG_ERROR: {
                      nlerr = NLMSG_DATA(nlmp);
                      LOGE("NLMSG error: %d", nlerr->error);
                      return nlerr->error;
                  }
              case RTM_NEWLINK: {
                      res = interface_from_rtm_newlink(nlmp);
                      break;
                  }

              case RTM_NEWADDR: {
                      res = interface_addresses_from_rtm_newaddr(nlmp);
                      break;
                  }
              default: {
                       LOGD("nlmsg_type unknown: %d", nlmp->nlmsg_type);
                  }
              }
              
              if (res != 0) {
                  return res;
              }
              
              status -= NLMSG_ALIGN(len);
              nlmp = (struct nlmsghdr*)((char*)nlmp + NLMSG_ALIGN(len));

	}
      } /* FOR next message */

}


int GetNetLinkInterfaces() {
    int netlink_socket;
    int res;
    struct NetworkInterface_struct *ni;
    struct ipAddress_struct *na, *address;
    rtm_header_universal head;
    int ii, ai, addresses, interfaces;

    LOGI ("NetworkInterfaces");
    memset(&addr_nl, 0, sizeof(struct sockaddr_nl));
    addr_nl.nl_family = AF_NETLINK;
    netlink_socket = socket(PF_NETLINK, SOCK_DGRAM, NETLINK_ROUTE);
    if (netlink_socket <= 0) {
        LOGE("Cannot create netlink socket");
        return -1;
    }
    if (bind(netlink_socket, (struct sockaddr *)(&addr_nl), 
             sizeof(struct sockaddr_nl))) {
        LOGE("Cannot bind netlink socket");
        return -1;
    }
    rtm_head_init(&head, rtm_link);
    head.rtm.ifinfo.ifi_family = AF_UNSPEC;
    res = get_interface_addresses(netlink_socket, RTM_GETLINK, &head);
    LOGI ("NetworkInterfaces phase 1 finished: %d", res);

    rtm_head_init(&head, rtm_addr);
    head.rtm.ifaddr.ifa_family = AF_UNSPEC;
    res = get_interface_addresses(netlink_socket, RTM_GETADDR, &head);
    LOGI ("NetworkInterfaces phase 2 finished: %d", res);
    close(netlink_socket);
    print_interfaces();

    /* caller must call free_network_interface() */
    return 0;
}






/**
 * Answer an array of NetworkInterface objects.  One for each network interface within the system
 *
 * @param      env     pointer to the JNI library
 * @param      clazz   the class of the object invoking the JNI function
 *
 * @return                     an array of NetworkInterface objects of length 0 or more
 */

static jobjectArray getNetworkInterfacesImpl(JNIEnv * env, jclass clazz) {

    /* variables to store network interfac edata returned by call to port library */
    int result = 0;

    /* variables for class and method objects needed to create bridge to java */
    jclass networkInterfaceClass = NULL;
    jclass inetAddressClass = NULL;
    jclass utilClass = NULL;
    jmethodID methodID = NULL;
    jmethodID utilMid = NULL;

    /* JNI objects used to return values from native call */
    jstring name = NULL;
    jstring displayName = NULL;
    jobjectArray addresses = NULL;
    jobjectArray networkInterfaces = NULL;
    jbyteArray bytearray = NULL;

    /* jobjects used to build the object arrays returned */
    jobject currentInterface = NULL;
    jobject element = NULL;

    /* misc variables needed for looping and determining inetAddress info */
    unsigned int i = 0;
    unsigned int j = 0;
    unsigned int nameLength = 0;
    unsigned int noofinterfaces;
    unsigned int noofaddr;
    struct NetworkInterface_struct *ni;
    struct ipAddress_struct *na;


    /* get the classes and methods that we need for later calls */
    networkInterfaceClass = (*env)->FindClass(env, "java/net/NetworkInterface");
    if(networkInterfaceClass == NULL) {
        throwSocketException(env, netLookupErrorString(SOCKERR_NORECOVERY));
        return NULL;
    }

    inetAddressClass = (*env)->FindClass(env, "java/net/InetAddress");
    if(inetAddressClass == NULL) {
        throwSocketException(env, netLookupErrorString(SOCKERR_NORECOVERY));
        return NULL;
    }

    methodID = (*env)->GetMethodID(env, networkInterfaceClass, "<init>",
            "(Ljava/lang/String;Ljava/lang/String;[Ljava/net/InetAddress;I)V");
    if(methodID == NULL) {
        throwSocketException(env, netLookupErrorString(SOCKERR_NORECOVERY));
        return NULL;
    }

    utilClass = (*env)->FindClass(env, "org/apache/harmony/luni/util/Util");
    if(!utilClass) {
        return NULL;
    }

    utilMid = ((*env)->GetStaticMethodID(env, utilClass, "toString",
            "([BII)Ljava/lang/String;"));
    if(!utilMid) {
        return NULL;
    }

    result = GetNetLinkInterfaces();
    if(result < 0) {
        /* this means an error occured. */
        throwSocketException(env, netLookupErrorString(result));
        return NULL;
    }

    /* count interfaces */
    noofinterfaces = 0;
    for (ni = network_interfaces; ni != NULL; ni = ni->nextinterface) {
         /* include only interfaces in state up */
         if ((ni->flags & IFF_UP) != 0) {
             noofinterfaces++;
         }
    } 

    /* now loop through the interfaces and extract the information to be returned */
    j = 0;
    for (ni = network_interfaces; ni != NULL; ni = ni->nextinterface) {
         /* include only interfaces in state up */
         if ((ni->flags & IFF_UP) != 0) {
            /* set the name and display name and reset the addresses object array */
            addresses = NULL;
            name = NULL;
            displayName = NULL;

            if(ni->name != NULL) {
                nameLength = strlen(ni->name);
                bytearray = (*env)->NewByteArray(env, nameLength);
                if(bytearray == NULL) {
                    /* NewByteArray should have thrown an exception */
                    return NULL;
                }
                (*env)->SetByteArrayRegion(env, bytearray, (jint) 0, nameLength, 
                                           (jbyte *)ni->name);
                name = (*env)->CallStaticObjectMethod(env, utilClass, utilMid,
                                                      bytearray, (jint) 0, nameLength);
                if((*env)->ExceptionCheck(env)) {
                    return NULL;
                }
            }

            if(ni->displayName != NULL) {
                nameLength = strlen(ni->displayName);
                bytearray = (*env)->NewByteArray(env, nameLength);
                if(bytearray == NULL) {
                    /* NewByteArray should have thrown an exception */
                    return NULL;
                }
                (*env)->SetByteArrayRegion(env, bytearray, (jint) 0, nameLength, 
                                           (jbyte *)ni->displayName);
                displayName = (*env)->CallStaticObjectMethod(env, utilClass, utilMid,
                                                             bytearray, (jint) 0, nameLength);
                if((*env)->ExceptionCheck(env)) {
                    return NULL;
                }
            }

            /* generate the object with the inet addresses for the interface       */
            /* count addresses */
            noofaddr = 0;
            for (na = ni->addresses; na != NULL; na = na->nextaddress) {
                noofaddr++;
            }
            na = ni->addresses;    
            if (na != NULL) {
                element = structInToInetAddress(env, (struct in_addr6 *) &na->addr, na->family);
                addresses = (*env)->NewObjectArray(env, noofaddr, inetAddressClass, element);
                i = 1;
                for (na = na->nextaddress; na != NULL; na = na->nextaddress) {
                    element = structInToInetAddress(env, (struct in_addr6 *) &na->addr, na->family);
                    (*env)->SetObjectArrayElement(env, addresses, i, element);
                    i++;
                 }
            }

            /* now  create the NetworkInterface object for this interface and then add it it ot the array that will be returned */
            currentInterface = (*env)->NewObject(env, networkInterfaceClass,
                methodID, name, displayName, addresses, ni->index);
            if(j == 0) {
                networkInterfaces = (*env)->NewObjectArray(env,
                        noofinterfaces, networkInterfaceClass,
                        currentInterface);
            } else {
                (*env)->SetObjectArrayElement(env, networkInterfaces, j, currentInterface);
            }
            j++;
        }
    }

    /* free the memory for the interfaces struct and return the new NetworkInterface List */
    free_network_interface();
    return networkInterfaces;
}


/*
 * JNI registration
 */
static JNINativeMethod gMethods[] = {
    /* name, signature, funcPtr */
    { "getNetworkInterfacesImpl", "()[Ljava/net/NetworkInterface;", getNetworkInterfacesImpl }
};
int register_java_net_NetworkInterface(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "java/net/NetworkInterface",
        gMethods, NELEM(gMethods));

}
