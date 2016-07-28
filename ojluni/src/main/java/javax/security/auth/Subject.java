/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javax.security.auth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.*;
import java.text.MessageFormat;
import java.security.AccessControlContext;

import java.security.Principal;
import java.security.PrivilegedActionException;
import sun.security.util.ResourcesMgr;

/**
 * <p> A {@code Subject} represents a grouping of related information
 * for a single entity, such as a person.
 * Such information includes the Subject's identities as well as
 * its security-related attributes
 * (passwords and cryptographic keys, for example).
 *
 * <p> Subjects may potentially have multiple identities.
 * Each identity is represented as a {@code Principal}
 * within the {@code Subject}.  Principals simply bind names to a
 * {@code Subject}.  For example, a {@code Subject} that happens
 * to be a person, Alice, might have two Principals:
 * one which binds "Alice Bar", the name on her driver license,
 * to the {@code Subject}, and another which binds,
 * "999-99-9999", the number on her student identification card,
 * to the {@code Subject}.  Both Principals refer to the same
 * {@code Subject} even though each has a different name.
 *
 * <p> A {@code Subject} may also own security-related attributes,
 * which are referred to as credentials.
 * Sensitive credentials that require special protection, such as
 * private cryptographic keys, are stored within a private credential
 * {@code Set}.  Credentials intended to be shared, such as
 * public key certificates or Kerberos server tickets are stored
 * within a public credential {@code Set}.  Different permissions
 * are required to access and modify the different credential Sets.
 *
 * <p> To retrieve all the Principals associated with a {@code Subject},
 * invoke the {@code getPrincipals} method.  To retrieve
 * all the public or private credentials belonging to a {@code Subject},
 * invoke the {@code getPublicCredentials} method or
 * {@code getPrivateCredentials} method, respectively.
 * To modify the returned {@code Set} of Principals and credentials,
 * use the methods defined in the {@code Set} class.
 * For example:
 * <pre>
 *      Subject subject;
 *      Principal principal;
 *      Object credential;
 *
 *      // add a Principal and credential to the Subject
 *      subject.getPrincipals().add(principal);
 *      subject.getPublicCredentials().add(credential);
 * </pre>
 *
 * <p> This {@code Subject} class implements {@code Serializable}.
 * While the Principals associated with the {@code Subject} are serialized,
 * the credentials associated with the {@code Subject} are not.
 * Note that the {@code java.security.Principal} class
 * does not implement {@code Serializable}.  Therefore all concrete
 * {@code Principal} implementations associated with Subjects
 * must implement {@code Serializable}.
 *
 * @see java.security.Principal
 * @see java.security.DomainCombiner
 */
public final class Subject implements java.io.Serializable {

    private static final long serialVersionUID = -8308522755600156056L;

    /**
     * A {@code Set} that provides a view of all of this
     * Subject's Principals
     *
     * <p>
     *
     * @serial Each element in this set is a
     *          {@code java.security.Principal}.
     *          The set is a {@code Subject.SecureSet}.
     */
    Set<Principal> principals;

    /**
     * Sets that provide a view of all of this
     * Subject's Credentials
     */
    transient Set<Object> pubCredentials;
    transient Set<Object> privCredentials;

    /**
     * Whether this Subject is read-only
     *
     * @serial
     */
    private volatile boolean readOnly = false;

    private static final int PRINCIPAL_SET = 1;
    private static final int PUB_CREDENTIAL_SET = 2;
    private static final int PRIV_CREDENTIAL_SET = 3;

    /**
     * Create an instance of a {@code Subject}
     * with an empty {@code Set} of Principals and empty
     * Sets of public and private credentials.
     *
     * <p> The newly constructed Sets check whether this {@code Subject}
     * has been set read-only before permitting subsequent modifications.
     * The newly created Sets also prevent illegal modifications
     * by ensuring that callers have sufficient permissions.
     *
     * <p> To modify the Principals Set, the caller must have
     * {@code AuthPermission("modifyPrincipals")}.
     * To modify the public credential Set, the caller must have
     * {@code AuthPermission("modifyPublicCredentials")}.
     * To modify the private credential Set, the caller must have
     * {@code AuthPermission("modifyPrivateCredentials")}.
     */
    public Subject() {

        this.principals = Collections.synchronizedSet
                        (new SecureSet<Principal>(this, PRINCIPAL_SET));
        this.pubCredentials = Collections.synchronizedSet
                        (new SecureSet<Object>(this, PUB_CREDENTIAL_SET));
        this.privCredentials = Collections.synchronizedSet
                        (new SecureSet<Object>(this, PRIV_CREDENTIAL_SET));
    }

    /**
     * Create an instance of a {@code Subject} with
     * Principals and credentials.
     *
     * <p> The Principals and credentials from the specified Sets
     * are copied into newly constructed Sets.
     * These newly created Sets check whether this {@code Subject}
     * has been set read-only before permitting subsequent modifications.
     * The newly created Sets also prevent illegal modifications
     * by ensuring that callers have sufficient permissions.
     *
     * <p> To modify the Principals Set, the caller must have
     * {@code AuthPermission("modifyPrincipals")}.
     * To modify the public credential Set, the caller must have
     * {@code AuthPermission("modifyPublicCredentials")}.
     * To modify the private credential Set, the caller must have
     * {@code AuthPermission("modifyPrivateCredentials")}.
     * <p>
     *
     * @param readOnly true if the {@code Subject} is to be read-only,
     *          and false otherwise. <p>
     *
     * @param principals the {@code Set} of Principals
     *          to be associated with this {@code Subject}. <p>
     *
     * @param pubCredentials the {@code Set} of public credentials
     *          to be associated with this {@code Subject}. <p>
     *
     * @param privCredentials the {@code Set} of private credentials
     *          to be associated with this {@code Subject}.
     *
     * @exception NullPointerException if the specified
     *          {@code principals}, {@code pubCredentials},
     *          or {@code privCredentials} are {@code null}.
     */
    public Subject(boolean readOnly, Set<? extends Principal> principals,
                   Set<?> pubCredentials, Set<?> privCredentials)
    {
        // Android changed: added braces. Couldn't resist.
        if (principals == null ||
            pubCredentials == null ||
            privCredentials == null) {
            throw new NullPointerException
                    (ResourcesMgr.getString("invalid.null.input.s."));
        }

        this.principals = Collections.synchronizedSet(new SecureSet<Principal>
                                (this, PRINCIPAL_SET, principals));
        this.pubCredentials = Collections.synchronizedSet(new SecureSet<Object>
                                (this, PUB_CREDENTIAL_SET, pubCredentials));
        this.privCredentials = Collections.synchronizedSet(new SecureSet<Object>
                                (this, PRIV_CREDENTIAL_SET, privCredentials));
        this.readOnly = readOnly;
    }

    /**
     * Set this {@code Subject} to be read-only.
     *
     * <p> Modifications (additions and removals) to this Subject's
     * {@code Principal} {@code Set} and
     * credential Sets will be disallowed.
     * The {@code destroy} operation on this Subject's credentials will
     * still be permitted.
     *
     * <p> Subsequent attempts to modify the Subject's {@code Principal}
     * and credential Sets will result in an
     * {@code IllegalStateException} being thrown.
     * Also, once a {@code Subject} is read-only,
     * it can not be reset to being writable again.
     */
    public void setReadOnly() {
        this.readOnly = true;
    }

    /**
     * Query whether this {@code Subject} is read-only.
     *
     * <p>
     *
     * @return true if this {@code Subject} is read-only, false otherwise.
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Legacy code; do not use.
     */
    public static Subject getSubject(final AccessControlContext acc) {
        return null;
    }

    /**
     * Legacy code; do not use
     *
     * <p>In Android it just runs the action passed as a parameter, regardless of the subject.
     *
     * <p>
     *
     * @param subject unused in Android. <p>
     *
     * @param <T> the type of the value returned by the PrivilegedAction's
     *                  {@code run} method.
     *
     * @param action the code to be run. <p>
     *
     * @return the value returned by the PrivilegedAction's
     *                  {@code run} method.
     *
     * @exception NullPointerException if the {@code PrivilegedAction}
     *                  is {@code null}. <p>
     */
    public static <T> T doAs(final Subject subject,
                        final java.security.PrivilegedAction<T> action) {
        if (action == null)
            throw new NullPointerException
                (ResourcesMgr.getString("invalid.null.action.provided"));

        // call doPrivileged and push this new context on the stack
        return java.security.AccessController.doPrivileged(
                action, null /* context: Ignored in Android */);
    }

    /**
     * Legacy code; do not use
     *
     * <p>In Android it just runs the action passed as a parameter, regardless of the subject.
     *
     * <p>
     *
     * @param subject unused in Android. <p>
     *
     * @param <T> the type of the value returned by the
     *                  PrivilegedExceptionAction's {@code run} method.
     *
     * @param action the code to be run as the specified
     *                  {@code Subject}. <p>
     *
     * @return the value returned by the
     *                  PrivilegedExceptionAction's {@code run} method.
     *
     * @exception PrivilegedActionException if the
     *                  {@code PrivilegedExceptionAction.run}
     *                  method throws a checked exception. <p>
     *
     * @exception NullPointerException if the specified
     *                  {@code PrivilegedExceptionAction} is
     *                  {@code null}. <p>
     */
    public static <T> T doAs(final Subject subject,
                        final java.security.PrivilegedExceptionAction<T> action)
                        throws java.security.PrivilegedActionException {
        if (action == null)
            throw new NullPointerException
                (ResourcesMgr.getString("invalid.null.action.provided"));

        // call doPrivileged and push this new context on the stack
        return java.security.AccessController.doPrivileged(
                action, null /* context. Ignored in Android */);
    }

    /**
     * Legacy code; do not use
     *
     * <p>In Android it just runs the action passed as a parameter, regardless of the subject.

     * <p>
     *
     * @param subject unused in Android. <p>
     *
     * @param <T> the type of the value returned by the PrivilegedAction's
     *                  {@code run} method.
     *
     * @param action the code to be run. <p>
     *
     * @param acc unused in Android. <p>
     *
     * @return the value returned by the PrivilegedAction's
     *                  {@code run} method.
     *
     * @exception NullPointerException if the {@code PrivilegedAction}
     *                  is {@code null}. <p>
     */
    public static <T> T doAsPrivileged(final Subject subject,
                        final java.security.PrivilegedAction<T> action,
                        final java.security.AccessControlContext acc) {
        if (action == null)
            throw new NullPointerException
                (ResourcesMgr.getString("invalid.null.action.provided"));

        // call doPrivileged and push this new context on the stack
        return java.security.AccessController.doPrivileged(
                action, null /* context. Ignored in Android */);
    }

    /**
     *  Legacy code; do not use
     *
     * <p>In Android it just runs the action passed as a parameter, regardless of the subject.

     * <p>
     *
     * @param subject unused in Android. <p>
     *
     * @param <T> the type of the value returned by the
     *                  PrivilegedExceptionAction's {@code run} method.
     *
     * @param action the code to be run as the specified
     *                  {@code Subject}. <p>
     *
     * @param acc unused in Android.. <p>
     *
     * @return the value returned by the
     *                  PrivilegedExceptionAction's {@code run} method.
     *
     * @exception PrivilegedActionException if the
     *                  {@code PrivilegedExceptionAction.run}
     *                  method throws a checked exception. <p>
     *
     * @exception NullPointerException if the specified
     *                  {@code PrivilegedExceptionAction} is
     *                  {@code null}. <p>
     */
    public static <T> T doAsPrivileged(final Subject subject,
                        final java.security.PrivilegedExceptionAction<T> action,
                        final java.security.AccessControlContext acc)
                        throws java.security.PrivilegedActionException {
        if (action == null)
            throw new NullPointerException
                (ResourcesMgr.getString("invalid.null.action.provided"));

        // call doPrivileged and push this new context on the stack
        return java.security.AccessController.doPrivileged(
                action, null /* context. Ignored in Android */);
    }

    /**
     * Return the {@code Set} of Principals associated with this
     * {@code Subject}.  Each {@code Principal} represents
     * an identity for this {@code Subject}.
     *
     * <p> The returned {@code Set} is backed by this Subject's
     * internal {@code Principal} {@code Set}.  Any modification
     * to the returned {@code Set} affects the internal
     * {@code Principal} {@code Set} as well.
     *
     * <p>
     *
     * @return  The {@code Set} of Principals associated with this
     *          {@code Subject}.
     */
    public Set<Principal> getPrincipals() {

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return principals;
    }

    /**
     * Return a {@code Set} of Principals associated with this
     * {@code Subject} that are instances or subclasses of the specified
     * {@code Class}.
     *
     * <p> The returned {@code Set} is not backed by this Subject's
     * internal {@code Principal} {@code Set}.  A new
     * {@code Set} is created and returned for each method invocation.
     * Modifications to the returned {@code Set}
     * will not affect the internal {@code Principal} {@code Set}.
     *
     * <p>
     *
     * @param <T> the type of the class modeled by {@code c}
     *
     * @param c the returned {@code Set} of Principals will all be
     *          instances of this class.
     *
     * @return a {@code Set} of Principals that are instances of the
     *          specified {@code Class}.
     *
     * @exception NullPointerException if the specified {@code Class}
     *                  is {@code null}.
     */
    public <T extends Principal> Set<T> getPrincipals(Class<T> c) {

        if (c == null)
            throw new NullPointerException
                (ResourcesMgr.getString("invalid.null.Class.provided"));

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet<T>(PRINCIPAL_SET, c);
    }

    /**
     * Return the {@code Set} of public credentials held by this
     * {@code Subject}.
     *
     * <p> The returned {@code Set} is backed by this Subject's
     * internal public Credential {@code Set}.  Any modification
     * to the returned {@code Set} affects the internal public
     * Credential {@code Set} as well.
     *
     * <p>
     *
     * @return  A {@code Set} of public credentials held by this
     *          {@code Subject}.
     */
    public Set<Object> getPublicCredentials() {

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return pubCredentials;
    }

    /**
     * Return the {@code Set} of private credentials held by this
     * {@code Subject}.
     *
     * <p> The returned {@code Set} is backed by this Subject's
     * internal private Credential {@code Set}.  Any modification
     * to the returned {@code Set} affects the internal private
     * Credential {@code Set} as well.
     *
     * <p> A caller requires permissions to access the Credentials
     * in the returned {@code Set}, or to modify the
     * {@code Set} itself.  A {@code SecurityException}
     * is thrown if the caller does not have the proper permissions.
     *
     * <p> While iterating through the {@code Set},
     * a {@code SecurityException} is thrown
     * if the caller does not have permission to access a
     * particular Credential.  The {@code Iterator}
     * is nevertheless advanced to next element in the {@code Set}.
     *
     * <p>
     *
     * @return  A {@code Set} of private credentials held by this
     *          {@code Subject}.
     */
    public Set<Object> getPrivateCredentials() {

        // XXX
        // we do not need a security check for
        // AuthPermission(getPrivateCredentials)
        // because we already restrict access to private credentials
        // via the PrivateCredentialPermission.  all the extra AuthPermission
        // would do is protect the set operations themselves
        // (like size()), which don't seem security-sensitive.

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return privCredentials;
    }

    /**
     * Return a {@code Set} of public credentials associated with this
     * {@code Subject} that are instances or subclasses of the specified
     * {@code Class}.
     *
     * <p> The returned {@code Set} is not backed by this Subject's
     * internal public Credential {@code Set}.  A new
     * {@code Set} is created and returned for each method invocation.
     * Modifications to the returned {@code Set}
     * will not affect the internal public Credential {@code Set}.
     *
     * <p>
     *
     * @param <T> the type of the class modeled by {@code c}
     *
     * @param c the returned {@code Set} of public credentials will all be
     *          instances of this class.
     *
     * @return a {@code Set} of public credentials that are instances
     *          of the  specified {@code Class}.
     *
     * @exception NullPointerException if the specified {@code Class}
     *          is {@code null}.
     */
    public <T> Set<T> getPublicCredentials(Class<T> c) {

        if (c == null)
            throw new NullPointerException
                (ResourcesMgr.getString("invalid.null.Class.provided"));

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet<T>(PUB_CREDENTIAL_SET, c);
    }

    /**
     * Return a {@code Set} of private credentials associated with this
     * {@code Subject} that are instances or subclasses of the specified
     * {@code Class}.
     *
     * <p> The caller must have permission to access all of the
     * requested Credentials, or a {@code SecurityException}
     * will be thrown.
     *
     * <p> The returned {@code Set} is not backed by this Subject's
     * internal private Credential {@code Set}.  A new
     * {@code Set} is created and returned for each method invocation.
     * Modifications to the returned {@code Set}
     * will not affect the internal private Credential {@code Set}.
     *
     * <p>
     *
     * @param <T> the type of the class modeled by {@code c}
     *
     * @param c the returned {@code Set} of private credentials will all be
     *          instances of this class.
     *
     * @return a {@code Set} of private credentials that are instances
     *          of the  specified {@code Class}.
     *
     * @exception NullPointerException if the specified {@code Class}
     *          is {@code null}.
     */
    public <T> Set<T> getPrivateCredentials(Class<T> c) {

        // XXX
        // we do not need a security check for
        // AuthPermission(getPrivateCredentials)
        // because we already restrict access to private credentials
        // via the PrivateCredentialPermission.  all the extra AuthPermission
        // would do is protect the set operations themselves
        // (like size()), which don't seem security-sensitive.

        if (c == null)
            throw new NullPointerException
                (ResourcesMgr.getString("invalid.null.Class.provided"));

        // always return an empty Set instead of null
        // so LoginModules can add to the Set if necessary
        return new ClassSet<T>(PRIV_CREDENTIAL_SET, c);
    }

    /**
     * Compares the specified Object with this {@code Subject}
     * for equality.  Returns true if the given object is also a Subject
     * and the two {@code Subject} instances are equivalent.
     * More formally, two {@code Subject} instances are
     * equal if their {@code Principal} and {@code Credential}
     * Sets are equal.
     *
     * <p>
     *
     * @param o Object to be compared for equality with this
     *          {@code Subject}.
     *
     * @return true if the specified Object is equal to this
     *          {@code Subject}.
     *
     * @exception SecurityException if the caller does not have permission
     *          to access the private credentials for this {@code Subject},
     *          or if the caller does not have permission to access the
     *          private credentials for the provided {@code Subject}.
     */
    public boolean equals(Object o) {

        if (o == null)
            return false;

        if (this == o)
            return true;

        if (o instanceof Subject) {

            final Subject that = (Subject)o;

            // check the principal and credential sets
            Set<Principal> thatPrincipals;
            synchronized(that.principals) {
                // avoid deadlock from dual locks
                thatPrincipals = new HashSet<Principal>(that.principals);
            }
            if (!principals.equals(thatPrincipals)) {
                return false;
            }

            Set<Object> thatPubCredentials;
            synchronized(that.pubCredentials) {
                // avoid deadlock from dual locks
                thatPubCredentials = new HashSet<Object>(that.pubCredentials);
            }
            if (!pubCredentials.equals(thatPubCredentials)) {
                return false;
            }

            Set<Object> thatPrivCredentials;
            synchronized(that.privCredentials) {
                // avoid deadlock from dual locks
                thatPrivCredentials = new HashSet<Object>(that.privCredentials);
            }
            if (!privCredentials.equals(thatPrivCredentials)) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Return the String representation of this {@code Subject}.
     *
     * <p>
     *
     * @return the String representation of this {@code Subject}.
     */
    public String toString() {
        return toString(true);
    }

    /**
     * package private convenience method to print out the Subject
     * without firing off a security check when trying to access
     * the Private Credentials
     */
    String toString(boolean includePrivateCredentials) {

        String s = ResourcesMgr.getString("Subject.");
        String suffix = "";

        synchronized(principals) {
            Iterator<Principal> pI = principals.iterator();
            while (pI.hasNext()) {
                Principal p = pI.next();
                suffix = suffix + ResourcesMgr.getString(".Principal.") +
                        p.toString() + ResourcesMgr.getString("NEWLINE");
            }
        }

        synchronized(pubCredentials) {
            Iterator<Object> pI = pubCredentials.iterator();
            while (pI.hasNext()) {
                Object o = pI.next();
                suffix = suffix +
                        ResourcesMgr.getString(".Public.Credential.") +
                        o.toString() + ResourcesMgr.getString("NEWLINE");
            }
        }

        if (includePrivateCredentials) {
            synchronized(privCredentials) {
                Iterator<Object> pI = privCredentials.iterator();
                while (pI.hasNext()) {
                    try {
                        Object o = pI.next();
                        suffix += ResourcesMgr.getString
                                        (".Private.Credential.") +
                                        o.toString() +
                                        ResourcesMgr.getString("NEWLINE");
                    } catch (SecurityException se) {
                        suffix += ResourcesMgr.getString
                                (".Private.Credential.inaccessible.");
                        break;
                    }
                }
            }
        }
        return s + suffix;
    }

    /**
     * Returns a hashcode for this {@code Subject}.
     *
     * <p>
     *
     * @return a hashcode for this {@code Subject}.
     *
     * @exception SecurityException if the caller does not have permission
     *          to access this Subject's private credentials.
     */
    public int hashCode() {

        /**
         * The hashcode is derived exclusive or-ing the
         * hashcodes of this Subject's Principals and credentials.
         *
         * If a particular credential was destroyed
         * ({@code credential.hashCode()} throws an
         * {@code IllegalStateException}),
         * the hashcode for that credential is derived via:
         * {@code credential.getClass().toString().hashCode()}.
         */

        int hashCode = 0;

        synchronized(principals) {
            Iterator<Principal> pIterator = principals.iterator();
            while (pIterator.hasNext()) {
                Principal p = pIterator.next();
                hashCode ^= p.hashCode();
            }
        }

        synchronized(pubCredentials) {
            Iterator<Object> pubCIterator = pubCredentials.iterator();
            while (pubCIterator.hasNext()) {
                hashCode ^= getCredHashCode(pubCIterator.next());
            }
        }
        return hashCode;
    }

    /**
     * get a credential's hashcode
     */
    private int getCredHashCode(Object o) {
        try {
            return o.hashCode();
        } catch (IllegalStateException ise) {
            return o.getClass().toString().hashCode();
        }
    }

    /**
     * Writes this object out to a stream (i.e., serializes it).
     */
    private void writeObject(java.io.ObjectOutputStream oos)
                throws java.io.IOException {
        synchronized(principals) {
            oos.defaultWriteObject();
        }
    }

    /**
     * Reads this object from a stream (i.e., deserializes it)
     */
    private void readObject(java.io.ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {

        s.defaultReadObject();

        // The Credential <code>Set</code> is not serialized, but we do not
        // want the default deserialization routine to set it to null.
        this.pubCredentials = Collections.synchronizedSet
                        (new SecureSet<Object>(this, PUB_CREDENTIAL_SET));
        this.privCredentials = Collections.synchronizedSet
                        (new SecureSet<Object>(this, PRIV_CREDENTIAL_SET));
    }

    /**
     * Prevent modifications unless caller has permission.
     *
     * @serial include
     */
    private static class SecureSet<E>
        extends AbstractSet<E>
        implements java.io.Serializable {

        private static final long serialVersionUID = 7911754171111800359L;

        /**
         * @serialField this$0 Subject The outer Subject instance.
         * @serialField elements LinkedList The elements in this set.
         */
        private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("this$0", Subject.class),
            new ObjectStreamField("elements", LinkedList.class),
            new ObjectStreamField("which", int.class)
        };

        Subject subject;
        LinkedList<E> elements;

        /**
         * @serial An integer identifying the type of objects contained
         *      in this set.  If {@code which == 1},
         *      this is a Principal set and all the elements are
         *      of type {@code java.security.Principal}.
         *      If {@code which == 2}, this is a public credential
         *      set and all the elements are of type {@code Object}.
         *      If {@code which == 3}, this is a private credential
         *      set and all the elements are of type {@code Object}.
         */
        private int which;

        SecureSet(Subject subject, int which) {
            this.subject = subject;
            this.which = which;
            this.elements = new LinkedList<E>();
        }

        SecureSet(Subject subject, int which, Set<? extends E> set) {
            this.subject = subject;
            this.which = which;
            this.elements = new LinkedList<E>(set);
        }

        public int size() {
            return elements.size();
        }

        public Iterator<E> iterator() {
            final LinkedList<E> list = elements;
            return new Iterator<E>() {
                ListIterator<E> i = list.listIterator(0);

                public boolean hasNext() {return i.hasNext();}

                public E next() {
                    if (which != Subject.PRIV_CREDENTIAL_SET) {
                        return i.next();
                    }

                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        try {
                            sm.checkPermission(new PrivateCredentialPermission
                                (list.get(i.nextIndex()).getClass().getName(),
                                subject.getPrincipals()));
                        } catch (SecurityException se) {
                            i.next();
                            throw (se);
                        }
                    }
                    return i.next();
                }

                public void remove() {
                    if (subject.isReadOnly()) {
                        throw new IllegalStateException(ResourcesMgr.getString
                                ("Subject.is.read.only"));
                    }

                    i.remove();
                }
            };
        }

        public boolean add(E o) {

            if (subject.isReadOnly()) {
                throw new IllegalStateException
                        (ResourcesMgr.getString("Subject.is.read.only"));
            }

            switch (which) {
            case Subject.PRINCIPAL_SET:
                if (!(o instanceof Principal)) {
                    throw new SecurityException(ResourcesMgr.getString
                        ("attempting.to.add.an.object.which.is.not.an.instance.of.java.security.Principal.to.a.Subject.s.Principal.Set"));
                }
                break;
            default:
                // ok to add Objects of any kind to credential sets
                break;
            }

            // check for duplicates
            if (!elements.contains(o))
                return elements.add(o);
            else
                return false;
        }

        public boolean remove(Object o) {

            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                E next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }

                if (next == null) {
                    if (o == null) {
                        e.remove();
                        return true;
                    }
                } else if (next.equals(o)) {
                    e.remove();
                    return true;
                }
            }
            return false;
        }

        public boolean contains(Object o) {
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                E next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }

                if (next == null) {
                    if (o == null) {
                        return true;
                    }
                } else if (next.equals(o)) {
                    return true;
                }
            }
            return false;
        }

        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                E next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }

                Iterator<?> ce = c.iterator();
                while (ce.hasNext()) {
                    Object o = ce.next();
                    if (next == null) {
                        if (o == null) {
                            e.remove();
                            modified = true;
                            break;
                        }
                    } else if (next.equals(o)) {
                        e.remove();
                        modified = true;
                        break;
                    }
                }
            }
            return modified;
        }

        public boolean retainAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            boolean retain = false;
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                retain = false;
                E next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }

                Iterator<?> ce = c.iterator();
                while (ce.hasNext()) {
                    Object o = ce.next();
                    if (next == null) {
                        if (o == null) {
                            retain = true;
                            break;
                        }
                    } else if (next.equals(o)) {
                        retain = true;
                        break;
                    }
                }

                if (!retain) {
                    e.remove();
                    retain = false;
                    modified = true;
                }
            }
            return modified;
        }

        public void clear() {
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                E next;
                if (which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }
                e.remove();
            }
        }

        /**
         * Writes this object out to a stream (i.e., serializes it).
         *
         * <p>
         *
         * @serialData If this is a private credential set,
         *      a security check is performed to ensure that
         *      the caller has permission to access each credential
         *      in the set.  If the security check passes,
         *      the set is serialized.
         */
        private void writeObject(java.io.ObjectOutputStream oos)
                throws java.io.IOException {

            if (which == Subject.PRIV_CREDENTIAL_SET) {
                // check permissions before serializing
                Iterator<E> i = iterator();
                while (i.hasNext()) {
                    i.next();
                }
            }
            ObjectOutputStream.PutField fields = oos.putFields();
            fields.put("this$0", subject);
            fields.put("elements", elements);
            fields.put("which", which);
            oos.writeFields();
        }

        private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException
        {
            ObjectInputStream.GetField fields = ois.readFields();
            subject = (Subject) fields.get("this$0", null);
            elements = (LinkedList<E>) fields.get("elements", null);
            which = fields.get("which", 0);
        }
    }

    /**
     * This class implements a {@code Set} which returns only
     * members that are an instance of a specified Class.
     */
    private class ClassSet<T> extends AbstractSet<T> {

        private int which;
        private Class<T> c;
        private Set<T> set;

        ClassSet(int which, Class<T> c) {
            this.which = which;
            this.c = c;
            set = new HashSet<T>();

            switch (which) {
            case Subject.PRINCIPAL_SET:
                synchronized(principals) { populateSet(); }
                break;
            case Subject.PUB_CREDENTIAL_SET:
                synchronized(pubCredentials) { populateSet(); }
                break;
            default:
                synchronized(privCredentials) { populateSet(); }
                break;
            }
        }

        @SuppressWarnings("unchecked")     /*To suppress warning from line 1374*/
        private void populateSet() {
            final Iterator<?> iterator;
            switch(which) {
            case Subject.PRINCIPAL_SET:
                iterator = Subject.this.principals.iterator();
                break;
            case Subject.PUB_CREDENTIAL_SET:
                iterator = Subject.this.pubCredentials.iterator();
                break;
            default:
                iterator = Subject.this.privCredentials.iterator();
                break;
            }

            // Check whether the caller has permisson to get
            // credentials of Class c

            while (iterator.hasNext()) {
                Object next;
                if (which == Subject.PRIV_CREDENTIAL_SET) {
                    next = java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedAction<Object>() {
                        public Object run() {
                            return iterator.next();
                        }
                    });
                } else {
                    next = iterator.next();
                }
                if (c.isAssignableFrom(next.getClass())) {
                    if (which != Subject.PRIV_CREDENTIAL_SET) {
                        set.add((T)next);
                    } else {
                        set.add((T)next);
                    }
                }
            }
        }

        public int size() {
            return set.size();
        }

        public Iterator<T> iterator() {
            return set.iterator();
        }

        public boolean add(T o) {

            if (!o.getClass().isAssignableFrom(c)) {
                MessageFormat form = new MessageFormat(ResourcesMgr.getString
                        ("attempting.to.add.an.object.which.is.not.an.instance.of.class"));
                Object[] source = {c.toString()};
                throw new SecurityException(form.format(source));
            }

            return set.add(o);
        }
    }
}
