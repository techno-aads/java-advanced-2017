package javax.sql.rowset;
import java.sql.Connection;
import javax.sql.RowSetListener;
import java.net.URL;
import java.io.InputStream;
import javax.sql.RowSetEvent;
import java.sql.Timestamp;
import java.sql.Blob;
import java.sql.NClob;
import java.sql.Array;
import java.sql.ResultSet;
import java.util.Map;
import java.sql.Time;
import java.sql.SQLType;
import java.sql.SQLWarning;
import javax.sql.RowSetMetaData;
import java.util.Collection;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.sql.Clob;
import javax.sql.RowSet;
import java.io.Reader;
import javax.sql.rowset.spi.SyncProvider;
import java.sql.SQLXML;
import java.sql.Date;
import java.sql.Savepoint;
import java.sql.RowId;
import java.util.Calendar;
import java.math.BigDecimal;
import java.sql.Ref;
public class CachedRowSetImpl implements CachedRowSet{
    public int size () {
        return 0;
}
    public void execute (java.sql.Connection arg0) {

}
    public void release () {

}
    public void acceptChanges () {

}
    public void acceptChanges (java.sql.Connection arg0) {

}
    public boolean columnUpdated (int arg0) {
        return false;
}
    public boolean columnUpdated (java.lang.String arg0) {
        return false;
}
    public void commit () {

}
    public javax.sql.rowset.CachedRowSet createCopy () {
        return null;
}
    public javax.sql.rowset.CachedRowSet createCopyNoConstraints () {
        return null;
}
    public javax.sql.rowset.CachedRowSet createCopySchema () {
        return null;
}
    public javax.sql.RowSet createShared () {
        return null;
}
    public int[] getKeyColumns () {
        return null;
}
    public java.sql.ResultSet getOriginal () {
        return null;
}
    public java.sql.ResultSet getOriginalRow () {
        return null;
}
    public int getPageSize () {
        return 0;
}
    public javax.sql.rowset.RowSetWarning getRowSetWarnings () {
        return null;
}
    public boolean getShowDeleted () {
        return false;
}
    public javax.sql.rowset.spi.SyncProvider getSyncProvider () {
        return null;
}
    public java.lang.String getTableName () {
        return null;
}
    public boolean nextPage () {
        return false;
}
    public void populate (java.sql.ResultSet arg0,int arg1) {

}
    public void populate (java.sql.ResultSet arg0) {

}
    public boolean previousPage () {
        return false;
}
    public void restoreOriginal () {

}
    public void rowSetPopulated (javax.sql.RowSetEvent arg0,int arg1) {

}
    public void setKeyColumns (int[] arg0) {

}
    public void setMetaData (javax.sql.RowSetMetaData arg0) {

}
    public void setOriginalRow () {

}
    public void setPageSize (int arg0) {

}
    public void setShowDeleted (boolean arg0) {

}
    public void setSyncProvider (java.lang.String arg0) {

}
    public void setTableName (java.lang.String arg0) {

}
    public java.util.Collection toCollection () {
        return null;
}
    public java.util.Collection toCollection (java.lang.String arg0) {
        return null;
}
    public java.util.Collection toCollection (int arg0) {
        return null;
}
    public void undoDelete () {

}
    public void undoInsert () {

}
    public void undoUpdate () {

}
    public void rollback (java.sql.Savepoint arg0) {

}
    public void rollback () {

}
    public void setURL (int arg0,java.net.URL arg1) {

}
    public void execute () {

}
    public void setReadOnly (boolean arg0) {

}
    public void setBoolean (java.lang.String arg0,boolean arg1) {

}
    public void setBoolean (int arg0,boolean arg1) {

}
    public void setByte (int arg0,byte arg1) {

}
    public void setByte (java.lang.String arg0,byte arg1) {

}
    public void setDouble (int arg0,double arg1) {

}
    public void setDouble (java.lang.String arg0,double arg1) {

}
    public void setFloat (int arg0,float arg1) {

}
    public void setFloat (java.lang.String arg0,float arg1) {

}
    public void setInt (java.lang.String arg0,int arg1) {

}
    public void setInt (int arg0,int arg1) {

}
    public void setLong (int arg0,long arg1) {

}
    public void setLong (java.lang.String arg0,long arg1) {

}
    public void setShort (int arg0,short arg1) {

}
    public void setShort (java.lang.String arg0,short arg1) {

}
    public boolean isReadOnly () {
        return false;
}
    public void setTimestamp (java.lang.String arg0,java.sql.Timestamp arg1) {

}
    public void setTimestamp (int arg0,java.sql.Timestamp arg1,java.util.Calendar arg2) {

}
    public void setTimestamp (int arg0,java.sql.Timestamp arg1) {

}
    public void setTimestamp (java.lang.String arg0,java.sql.Timestamp arg1,java.util.Calendar arg2) {

}
    public void setTime (int arg0,java.sql.Time arg1) {

}
    public void setTime (java.lang.String arg0,java.sql.Time arg1,java.util.Calendar arg2) {

}
    public void setTime (int arg0,java.sql.Time arg1,java.util.Calendar arg2) {

}
    public void setTime (java.lang.String arg0,java.sql.Time arg1) {

}
    public java.lang.String getPassword () {
        return null;
}
    public void setType (int arg0) {

}
    public void setDate (java.lang.String arg0,java.sql.Date arg1) {

}
    public void setDate (int arg0,java.sql.Date arg1,java.util.Calendar arg2) {

}
    public void setDate (java.lang.String arg0,java.sql.Date arg1,java.util.Calendar arg2) {

}
    public void setDate (int arg0,java.sql.Date arg1) {

}
    public void addRowSetListener (javax.sql.RowSetListener arg0) {

}
    public void clearParameters () {

}
    public java.lang.String getCommand () {
        return null;
}
    public java.lang.String getDataSourceName () {
        return null;
}
    public boolean getEscapeProcessing () {
        return false;
}
    public int getMaxFieldSize () {
        return 0;
}
    public int getMaxRows () {
        return 0;
}
    public int getQueryTimeout () {
        return 0;
}
    public int getTransactionIsolation () {
        return 0;
}
    public java.util.Map getTypeMap () {
        return null;
}
    public java.lang.String getUrl () {
        return null;
}
    public java.lang.String getUsername () {
        return null;
}
    public void removeRowSetListener (javax.sql.RowSetListener arg0) {

}
    public void setArray (int arg0,java.sql.Array arg1) {

}
    public void setAsciiStream (int arg0,java.io.InputStream arg1) {

}
    public void setAsciiStream (java.lang.String arg0,java.io.InputStream arg1,int arg2) {

}
    public void setAsciiStream (int arg0,java.io.InputStream arg1,int arg2) {

}
    public void setAsciiStream (java.lang.String arg0,java.io.InputStream arg1) {

}
    public void setBigDecimal (int arg0,java.math.BigDecimal arg1) {

}
    public void setBigDecimal (java.lang.String arg0,java.math.BigDecimal arg1) {

}
    public void setBinaryStream (java.lang.String arg0,java.io.InputStream arg1) {

}
    public void setBinaryStream (int arg0,java.io.InputStream arg1) {

}
    public void setBinaryStream (int arg0,java.io.InputStream arg1,int arg2) {

}
    public void setBinaryStream (java.lang.String arg0,java.io.InputStream arg1,int arg2) {

}
    public void setBlob (java.lang.String arg0,java.sql.Blob arg1) {

}
    public void setBlob (java.lang.String arg0,java.io.InputStream arg1) {

}
    public void setBlob (int arg0,java.io.InputStream arg1) {

}
    public void setBlob (java.lang.String arg0,java.io.InputStream arg1,long arg2) {

}
    public void setBlob (int arg0,java.sql.Blob arg1) {

}
    public void setBlob (int arg0,java.io.InputStream arg1,long arg2) {

}
    public void setBytes (java.lang.String arg0,byte[] arg1) {

}
    public void setBytes (int arg0,byte[] arg1) {

}
    public void setCharacterStream (java.lang.String arg0,java.io.Reader arg1,int arg2) {

}
    public void setCharacterStream (int arg0,java.io.Reader arg1,int arg2) {

}
    public void setCharacterStream (java.lang.String arg0,java.io.Reader arg1) {

}
    public void setCharacterStream (int arg0,java.io.Reader arg1) {

}
    public void setClob (java.lang.String arg0,java.sql.Clob arg1) {

}
    public void setClob (java.lang.String arg0,java.io.Reader arg1,long arg2) {

}
    public void setClob (int arg0,java.io.Reader arg1) {

}
    public void setClob (int arg0,java.io.Reader arg1,long arg2) {

}
    public void setClob (int arg0,java.sql.Clob arg1) {

}
    public void setClob (java.lang.String arg0,java.io.Reader arg1) {

}
    public void setCommand (java.lang.String arg0) {

}
    public void setConcurrency (int arg0) {

}
    public void setDataSourceName (java.lang.String arg0) {

}
    public void setEscapeProcessing (boolean arg0) {

}
    public void setMaxFieldSize (int arg0) {

}
    public void setMaxRows (int arg0) {

}
    public void setNCharacterStream (int arg0,java.io.Reader arg1,long arg2) {

}
    public void setNCharacterStream (java.lang.String arg0,java.io.Reader arg1,long arg2) {

}
    public void setNCharacterStream (int arg0,java.io.Reader arg1) {

}
    public void setNCharacterStream (java.lang.String arg0,java.io.Reader arg1) {

}
    public void setNClob (int arg0,java.sql.NClob arg1) {

}
    public void setNClob (java.lang.String arg0,java.io.Reader arg1) {

}
    public void setNClob (int arg0,java.io.Reader arg1) {

}
    public void setNClob (java.lang.String arg0,java.io.Reader arg1,long arg2) {

}
    public void setNClob (java.lang.String arg0,java.sql.NClob arg1) {

}
    public void setNClob (int arg0,java.io.Reader arg1,long arg2) {

}
    public void setNString (int arg0,java.lang.String arg1) {

}
    public void setNString (java.lang.String arg0,java.lang.String arg1) {

}
    public void setNull (java.lang.String arg0,int arg1,java.lang.String arg2) {

}
    public void setNull (java.lang.String arg0,int arg1) {

}
    public void setNull (int arg0,int arg1,java.lang.String arg2) {

}
    public void setNull (int arg0,int arg1) {

}
    public void setObject (java.lang.String arg0,java.lang.Object arg1,int arg2) {

}
    public void setObject (int arg0,java.lang.Object arg1,int arg2) {

}
    public void setObject (int arg0,java.lang.Object arg1,int arg2,int arg3) {

}
    public void setObject (java.lang.String arg0,java.lang.Object arg1,int arg2,int arg3) {

}
    public void setObject (int arg0,java.lang.Object arg1) {

}
    public void setObject (java.lang.String arg0,java.lang.Object arg1) {

}
    public void setPassword (java.lang.String arg0) {

}
    public void setQueryTimeout (int arg0) {

}
    public void setRef (int arg0,java.sql.Ref arg1) {

}
    public void setRowId (java.lang.String arg0,java.sql.RowId arg1) {

}
    public void setRowId (int arg0,java.sql.RowId arg1) {

}
    public void setSQLXML (int arg0,java.sql.SQLXML arg1) {

}
    public void setSQLXML (java.lang.String arg0,java.sql.SQLXML arg1) {

}
    public void setString (java.lang.String arg0,java.lang.String arg1) {

}
    public void setString (int arg0,java.lang.String arg1) {

}
    public void setTransactionIsolation (int arg0) {

}
    public void setTypeMap (java.util.Map arg0) {

}
    public void setUrl (java.lang.String arg0) {

}
    public void setUsername (java.lang.String arg0) {

}
    public void updateBytes (java.lang.String arg0,byte[] arg1) {

}
    public void updateBytes (int arg0,byte[] arg1) {

}
    public java.lang.Object getObject (int arg0,java.util.Map arg1) {
        return null;
}
    public java.lang.Object getObject (java.lang.String arg0) {
        return null;
}
    public java.lang.Object getObject (int arg0) {
        return null;
}
    public java.lang.Object getObject (java.lang.String arg0,java.util.Map arg1) {
        return null;
}
    public java.lang.Object getObject (int arg0,java.lang.Class arg1) {
        return null;
}
    public java.lang.Object getObject (java.lang.String arg0,java.lang.Class arg1) {
        return null;
}
    public boolean getBoolean (int arg0) {
        return false;
}
    public boolean getBoolean (java.lang.String arg0) {
        return false;
}
    public byte getByte (java.lang.String arg0) {
        return 0;
}
    public byte getByte (int arg0) {
        return 0;
}
    public short getShort (int arg0) {
        return 0;
}
    public short getShort (java.lang.String arg0) {
        return 0;
}
    public int getInt (int arg0) {
        return 0;
}
    public int getInt (java.lang.String arg0) {
        return 0;
}
    public long getLong (java.lang.String arg0) {
        return 0;
}
    public long getLong (int arg0) {
        return 0;
}
    public float getFloat (java.lang.String arg0) {
        return 0;
}
    public float getFloat (int arg0) {
        return 0;
}
    public double getDouble (java.lang.String arg0) {
        return 0;
}
    public double getDouble (int arg0) {
        return 0;
}
    public byte[] getBytes (int arg0) {
        return null;
}
    public byte[] getBytes (java.lang.String arg0) {
        return null;
}
    public boolean next () {
        return false;
}
    public java.sql.Array getArray (int arg0) {
        return null;
}
    public java.sql.Array getArray (java.lang.String arg0) {
        return null;
}
    public java.net.URL getURL (java.lang.String arg0) {
        return null;
}
    public java.net.URL getURL (int arg0) {
        return null;
}
    public boolean first () {
        return false;
}
    public void close () {

}
    public int getType () {
        return 0;
}
    public java.sql.Ref getRef (int arg0) {
        return null;
}
    public java.sql.Ref getRef (java.lang.String arg0) {
        return null;
}
    public boolean previous () {
        return false;
}
    public java.sql.Time getTime (int arg0) {
        return null;
}
    public java.sql.Time getTime (java.lang.String arg0) {
        return null;
}
    public java.sql.Time getTime (java.lang.String arg0,java.util.Calendar arg1) {
        return null;
}
    public java.sql.Time getTime (int arg0,java.util.Calendar arg1) {
        return null;
}
    public boolean last () {
        return false;
}
    public java.sql.Date getDate (int arg0) {
        return null;
}
    public java.sql.Date getDate (java.lang.String arg0) {
        return null;
}
    public java.sql.Date getDate (java.lang.String arg0,java.util.Calendar arg1) {
        return null;
}
    public java.sql.Date getDate (int arg0,java.util.Calendar arg1) {
        return null;
}
    public boolean isClosed () {
        return false;
}
    public java.sql.Timestamp getTimestamp (int arg0) {
        return null;
}
    public java.sql.Timestamp getTimestamp (java.lang.String arg0) {
        return null;
}
    public java.sql.Timestamp getTimestamp (java.lang.String arg0,java.util.Calendar arg1) {
        return null;
}
    public java.sql.Timestamp getTimestamp (int arg0,java.util.Calendar arg1) {
        return null;
}
    public java.lang.String getString (int arg0) {
        return null;
}
    public java.lang.String getString (java.lang.String arg0) {
        return null;
}
    public boolean absolute (int arg0) {
        return false;
}
    public void afterLast () {

}
    public void beforeFirst () {

}
    public void cancelRowUpdates () {

}
    public void clearWarnings () {

}
    public void deleteRow () {

}
    public int findColumn (java.lang.String arg0) {
        return 0;
}
    public java.io.InputStream getAsciiStream (java.lang.String arg0) {
        return null;
}
    public java.io.InputStream getAsciiStream (int arg0) {
        return null;
}
    public java.math.BigDecimal getBigDecimal (java.lang.String arg0,int arg1) {
        return null;
}
    public java.math.BigDecimal getBigDecimal (int arg0) {
        return null;
}
    public java.math.BigDecimal getBigDecimal (java.lang.String arg0) {
        return null;
}
    public java.math.BigDecimal getBigDecimal (int arg0,int arg1) {
        return null;
}
    public java.io.InputStream getBinaryStream (int arg0) {
        return null;
}
    public java.io.InputStream getBinaryStream (java.lang.String arg0) {
        return null;
}
    public java.sql.Blob getBlob (int arg0) {
        return null;
}
    public java.sql.Blob getBlob (java.lang.String arg0) {
        return null;
}
    public java.io.Reader getCharacterStream (java.lang.String arg0) {
        return null;
}
    public java.io.Reader getCharacterStream (int arg0) {
        return null;
}
    public java.sql.Clob getClob (int arg0) {
        return null;
}
    public java.sql.Clob getClob (java.lang.String arg0) {
        return null;
}
    public int getConcurrency () {
        return 0;
}
    public java.lang.String getCursorName () {
        return null;
}
    public int getFetchDirection () {
        return 0;
}
    public int getFetchSize () {
        return 0;
}
    public int getHoldability () {
        return 0;
}
    public java.sql.ResultSetMetaData getMetaData () {
        return null;
}
    public java.io.Reader getNCharacterStream (java.lang.String arg0) {
        return null;
}
    public java.io.Reader getNCharacterStream (int arg0) {
        return null;
}
    public java.sql.NClob getNClob (java.lang.String arg0) {
        return null;
}
    public java.sql.NClob getNClob (int arg0) {
        return null;
}
    public java.lang.String getNString (java.lang.String arg0) {
        return null;
}
    public java.lang.String getNString (int arg0) {
        return null;
}
    public int getRow () {
        return 0;
}
    public java.sql.RowId getRowId (java.lang.String arg0) {
        return null;
}
    public java.sql.RowId getRowId (int arg0) {
        return null;
}
    public java.sql.SQLXML getSQLXML (java.lang.String arg0) {
        return null;
}
    public java.sql.SQLXML getSQLXML (int arg0) {
        return null;
}
    public java.io.InputStream getUnicodeStream (int arg0) {
        return null;
}
    public java.io.InputStream getUnicodeStream (java.lang.String arg0) {
        return null;
}
    public java.sql.SQLWarning getWarnings () {
        return null;
}
    public void insertRow () {

}
    public boolean isAfterLast () {
        return false;
}
    public boolean isBeforeFirst () {
        return false;
}
    public boolean isLast () {
        return false;
}
    public void moveToCurrentRow () {

}
    public void moveToInsertRow () {

}
    public void refreshRow () {

}
    public boolean relative (int arg0) {
        return false;
}
    public boolean rowDeleted () {
        return false;
}
    public boolean rowInserted () {
        return false;
}
    public boolean rowUpdated () {
        return false;
}
    public void setFetchDirection (int arg0) {

}
    public void setFetchSize (int arg0) {

}
    public void updateArray (int arg0,java.sql.Array arg1) {

}
    public void updateArray (java.lang.String arg0,java.sql.Array arg1) {

}
    public void updateAsciiStream (int arg0,java.io.InputStream arg1,int arg2) {

}
    public void updateAsciiStream (java.lang.String arg0,java.io.InputStream arg1) {

}
    public void updateAsciiStream (int arg0,java.io.InputStream arg1) {

}
    public void updateAsciiStream (java.lang.String arg0,java.io.InputStream arg1,long arg2) {

}
    public void updateAsciiStream (int arg0,java.io.InputStream arg1,long arg2) {

}
    public void updateAsciiStream (java.lang.String arg0,java.io.InputStream arg1,int arg2) {

}
    public void updateBigDecimal (int arg0,java.math.BigDecimal arg1) {

}
    public void updateBigDecimal (java.lang.String arg0,java.math.BigDecimal arg1) {

}
    public void updateBinaryStream (java.lang.String arg0,java.io.InputStream arg1,int arg2) {

}
    public void updateBinaryStream (java.lang.String arg0,java.io.InputStream arg1,long arg2) {

}
    public void updateBinaryStream (java.lang.String arg0,java.io.InputStream arg1) {

}
    public void updateBinaryStream (int arg0,java.io.InputStream arg1,long arg2) {

}
    public void updateBinaryStream (int arg0,java.io.InputStream arg1,int arg2) {

}
    public void updateBinaryStream (int arg0,java.io.InputStream arg1) {

}
    public void updateBlob (int arg0,java.sql.Blob arg1) {

}
    public void updateBlob (java.lang.String arg0,java.sql.Blob arg1) {

}
    public void updateBlob (int arg0,java.io.InputStream arg1,long arg2) {

}
    public void updateBlob (int arg0,java.io.InputStream arg1) {

}
    public void updateBlob (java.lang.String arg0,java.io.InputStream arg1) {

}
    public void updateBlob (java.lang.String arg0,java.io.InputStream arg1,long arg2) {

}
    public void updateBoolean (int arg0,boolean arg1) {

}
    public void updateBoolean (java.lang.String arg0,boolean arg1) {

}
    public void updateByte (int arg0,byte arg1) {

}
    public void updateByte (java.lang.String arg0,byte arg1) {

}
    public void updateCharacterStream (int arg0,java.io.Reader arg1) {

}
    public void updateCharacterStream (int arg0,java.io.Reader arg1,long arg2) {

}
    public void updateCharacterStream (java.lang.String arg0,java.io.Reader arg1,int arg2) {

}
    public void updateCharacterStream (java.lang.String arg0,java.io.Reader arg1) {

}
    public void updateCharacterStream (int arg0,java.io.Reader arg1,int arg2) {

}
    public void updateCharacterStream (java.lang.String arg0,java.io.Reader arg1,long arg2) {

}
    public void updateClob (int arg0,java.io.Reader arg1,long arg2) {

}
    public void updateClob (int arg0,java.io.Reader arg1) {

}
    public void updateClob (java.lang.String arg0,java.io.Reader arg1) {

}
    public void updateClob (java.lang.String arg0,java.io.Reader arg1,long arg2) {

}
    public void updateClob (java.lang.String arg0,java.sql.Clob arg1) {

}
    public void updateClob (int arg0,java.sql.Clob arg1) {

}
    public void updateDate (int arg0,java.sql.Date arg1) {

}
    public void updateDate (java.lang.String arg0,java.sql.Date arg1) {

}
    public void updateDouble (int arg0,double arg1) {

}
    public void updateDouble (java.lang.String arg0,double arg1) {

}
    public void updateFloat (int arg0,float arg1) {

}
    public void updateFloat (java.lang.String arg0,float arg1) {

}
    public void updateInt (java.lang.String arg0,int arg1) {

}
    public void updateInt (int arg0,int arg1) {

}
    public void updateLong (java.lang.String arg0,long arg1) {

}
    public void updateLong (int arg0,long arg1) {

}
    public void updateNCharacterStream (int arg0,java.io.Reader arg1,long arg2) {

}
    public void updateNCharacterStream (java.lang.String arg0,java.io.Reader arg1,long arg2) {

}
    public void updateNCharacterStream (int arg0,java.io.Reader arg1) {

}
    public void updateNCharacterStream (java.lang.String arg0,java.io.Reader arg1) {

}
    public void updateNClob (int arg0,java.io.Reader arg1) {

}
    public void updateNClob (java.lang.String arg0,java.io.Reader arg1) {

}
    public void updateNClob (java.lang.String arg0,java.io.Reader arg1,long arg2) {

}
    public void updateNClob (int arg0,java.io.Reader arg1,long arg2) {

}
    public void updateNClob (java.lang.String arg0,java.sql.NClob arg1) {

}
    public void updateNClob (int arg0,java.sql.NClob arg1) {

}
    public void updateNString (int arg0,java.lang.String arg1) {

}
    public void updateNString (java.lang.String arg0,java.lang.String arg1) {

}
    public void updateNull (int arg0) {

}
    public void updateNull (java.lang.String arg0) {

}
    public void updateObject (int arg0,java.lang.Object arg1,java.sql.SQLType arg2) {

}
    public void updateObject (java.lang.String arg0,java.lang.Object arg1,java.sql.SQLType arg2) {

}
    public void updateObject (int arg0,java.lang.Object arg1,java.sql.SQLType arg2,int arg3) {

}
    public void updateObject (java.lang.String arg0,java.lang.Object arg1,java.sql.SQLType arg2,int arg3) {

}
    public void updateObject (int arg0,java.lang.Object arg1,int arg2) {

}
    public void updateObject (java.lang.String arg0,java.lang.Object arg1) {

}
    public void updateObject (int arg0,java.lang.Object arg1) {

}
    public void updateObject (java.lang.String arg0,java.lang.Object arg1,int arg2) {

}
    public void updateRef (int arg0,java.sql.Ref arg1) {

}
    public void updateRef (java.lang.String arg0,java.sql.Ref arg1) {

}
    public void updateRow () {

}
    public void updateRowId (java.lang.String arg0,java.sql.RowId arg1) {

}
    public void updateRowId (int arg0,java.sql.RowId arg1) {

}
    public void updateSQLXML (int arg0,java.sql.SQLXML arg1) {

}
    public void updateSQLXML (java.lang.String arg0,java.sql.SQLXML arg1) {

}
    public void updateShort (int arg0,short arg1) {

}
    public void updateShort (java.lang.String arg0,short arg1) {

}
    public void updateString (int arg0,java.lang.String arg1) {

}
    public void updateString (java.lang.String arg0,java.lang.String arg1) {

}
    public void updateTime (int arg0,java.sql.Time arg1) {

}
    public void updateTime (java.lang.String arg0,java.sql.Time arg1) {

}
    public void updateTimestamp (java.lang.String arg0,java.sql.Timestamp arg1) {

}
    public void updateTimestamp (int arg0,java.sql.Timestamp arg1) {

}
    public boolean wasNull () {
        return false;
}
    public java.sql.Statement getStatement () {
        return null;
}
    public boolean isFirst () {
        return false;
}
    public java.lang.Object unwrap (java.lang.Class arg0) {
        return null;
}
    public boolean isWrapperFor (java.lang.Class arg0) {
        return false;
}
    public int[] getMatchColumnIndexes () {
        return null;
}
    public java.lang.String[] getMatchColumnNames () {
        return null;
}
    public void setMatchColumn (int arg0) {

}
    public void setMatchColumn (java.lang.String[] arg0) {

}
    public void setMatchColumn (java.lang.String arg0) {

}
    public void setMatchColumn (int[] arg0) {

}
    public void unsetMatchColumn (java.lang.String arg0) {

}
    public void unsetMatchColumn (java.lang.String[] arg0) {

}
    public void unsetMatchColumn (int[] arg0) {

}
    public void unsetMatchColumn (int arg0) {

}
}