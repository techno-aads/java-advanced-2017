package org.omg.DynamicAny;
import org.omg.CORBA.NVList;
import org.omg.CORBA.SetOverrideType;
import org.omg.CORBA.Object;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Request;
import java.io.Serializable;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.Any;
import org.omg.CORBA.Context;
import org.omg.CORBA.Policy;
public class DynAnyImpl implements DynAny{
    public org.omg.CORBA.TypeCode type () {
        return null;
}
    public boolean next () {
        return false;
}
    public void destroy () {

}
    public org.omg.DynamicAny.DynAny copy () {
        return null;
}
    public void rewind () {

}
    public boolean equal (org.omg.DynamicAny.DynAny arg0) {
        return false;
}
    public int component_count () {
        return 0;
}
    public org.omg.DynamicAny.DynAny current_component () {
        return null;
}
    public void from_any (org.omg.CORBA.Any arg0) {

}
    public org.omg.CORBA.Any get_any () {
        return null;
}
    public boolean get_boolean () {
        return false;
}
    public char get_char () {
        return '\0';
}
    public double get_double () {
        return 0;
}
    public org.omg.DynamicAny.DynAny get_dyn_any () {
        return null;
}
    public float get_float () {
        return 0;
}
    public int get_long () {
        return 0;
}
    public long get_longlong () {
        return 0;
}
    public byte get_octet () {
        return 0;
}
    public org.omg.CORBA.Object get_reference () {
        return null;
}
    public short get_short () {
        return 0;
}
    public java.lang.String get_string () {
        return null;
}
    public org.omg.CORBA.TypeCode get_typecode () {
        return null;
}
    public int get_ulong () {
        return 0;
}
    public long get_ulonglong () {
        return 0;
}
    public short get_ushort () {
        return 0;
}
    public java.io.Serializable get_val () {
        return null;
}
    public char get_wchar () {
        return '\0';
}
    public java.lang.String get_wstring () {
        return null;
}
    public void insert_any (org.omg.CORBA.Any arg0) {

}
    public void insert_boolean (boolean arg0) {

}
    public void insert_char (char arg0) {

}
    public void insert_double (double arg0) {

}
    public void insert_dyn_any (org.omg.DynamicAny.DynAny arg0) {

}
    public void insert_float (float arg0) {

}
    public void insert_long (int arg0) {

}
    public void insert_longlong (long arg0) {

}
    public void insert_octet (byte arg0) {

}
    public void insert_reference (org.omg.CORBA.Object arg0) {

}
    public void insert_short (short arg0) {

}
    public void insert_string (java.lang.String arg0) {

}
    public void insert_typecode (org.omg.CORBA.TypeCode arg0) {

}
    public void insert_ulong (int arg0) {

}
    public void insert_ulonglong (long arg0) {

}
    public void insert_ushort (short arg0) {

}
    public void insert_val (java.io.Serializable arg0) {

}
    public void insert_wchar (char arg0) {

}
    public void insert_wstring (java.lang.String arg0) {

}
    public org.omg.CORBA.Any to_any () {
        return null;
}
    public boolean seek (int arg0) {
        return false;
}
    public void assign (org.omg.DynamicAny.DynAny arg0) {

}
    public org.omg.CORBA.Request _create_request (org.omg.CORBA.Context arg0,java.lang.String arg1,org.omg.CORBA.NVList arg2,org.omg.CORBA.NamedValue arg3,org.omg.CORBA.ExceptionList arg4,org.omg.CORBA.ContextList arg5) {
        return null;
}
    public org.omg.CORBA.Request _create_request (org.omg.CORBA.Context arg0,java.lang.String arg1,org.omg.CORBA.NVList arg2,org.omg.CORBA.NamedValue arg3) {
        return null;
}
    public org.omg.CORBA.Object _duplicate () {
        return null;
}
    public org.omg.CORBA.DomainManager[] _get_domain_managers () {
        return null;
}
    public org.omg.CORBA.Object _get_interface_def () {
        return null;
}
    public org.omg.CORBA.Policy _get_policy (int arg0) {
        return null;
}
    public int _hash (int arg0) {
        return 0;
}
    public boolean _is_a (java.lang.String arg0) {
        return false;
}
    public boolean _is_equivalent (org.omg.CORBA.Object arg0) {
        return false;
}
    public boolean _non_existent () {
        return false;
}
    public void _release () {

}
    public org.omg.CORBA.Request _request (java.lang.String arg0) {
        return null;
}
    public org.omg.CORBA.Object _set_policy_override (org.omg.CORBA.Policy[] arg0,org.omg.CORBA.SetOverrideType arg1) {
        return null;
}
}