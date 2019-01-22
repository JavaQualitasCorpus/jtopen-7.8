///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLResultSetTableModel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;



/**
The SQLResultSetTableModel class represents the data in a JDBC
result set generated by an SQL query.  This class implements the
underlying model for a table in a graphical user interface.
Use this class if you want to customize the graphical user interface
that presents a table.  If you do not need to customize the graphical user
interface, then use <a href="SQLResultSetTablePane.html">
SQLResultSetTablePane</a> instead.

<p>You must specify an <a href="SQLConnection.html">
SQLConnection</a> object and SQL query string to use for generating
the data.  Alternately, you can specify a ResultSet object directly.
If you specify a ResultSet object, it will override any SQLConnection
or SQL query previously set.  In addition, if you specify a ResultSet,
this class will use memory more efficiently if you create the ResultSet as scrollable.

<p>You must explicitly call <a href="#load()">load()</a> to load the information
from the result set.  The model will be empty until load() is called.
If the query or result set includes updatable columns, then the respective
columns will be editable.

<p>This class assumes that the necessary JDBC driver(s) are already registered.

<p>Call <a href="#close()">close()</a> to ensure that the result set
is closed when this table is no longer needed.

<p>Most errors are reported as <a href="ErrorEvent.html">ErrorEvent</a>s
rather than throwing exceptions.  Listen for ErrorEvents in order to diagnose and recover
from error conditions.

<p>SQLResultSetTableModel objects generate the following events:
<ul>
  <li>ErrorEvent
  <li>PropertyChangeEvent
  <li>TableModelEvent
  <li>WorkingEvent
</ul>

<p>This example creates an SQLResultSetTableModel using an SQLConnection
and query and displays it using a JTable:

<pre>
// Register the IBM Toolbox for Java JDBC driver.
DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
<br>
// Create the SQLResultSetTableModel object.
SQLConnection connection = new SQLConnection("jdbc:as400://MySystem", "Userid", "Password");
String query = "SELECT * FROM MYLIB.MYTABLE";
SQLResultSetTableModel model = new SQLResultSetTableModel(connection, query);
<br>
// Create the enclosing JTable and put it in a JFrame.
JTable table = new JTable(model);
JFrame frame = new JFrame("My Window");
frame.getContentPane().add(new JScrollPane(table));
<br>
// Set up the error dialog adapter.
model.addErrorListener(new ErrorDialogAdapter(frame));
<br>
// Display the JFrame.
frame.pack();
frame.show();
<br>
// Load the contents of the model.
model.load();
</pre>

<p>This example creates an SQLResultSetTableModel using a ResultSet
and displays it using a JTable:

<pre>
// Register the IBM Toolbox for Java JDBC driver.
DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
<br>
// Use JDBC to execute the SQL query directly.
Connection connection = DriverManager.getConnection("MySystem", "Userid", "Password");
Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
ResultSet rs = statement.executeQuery("SELECT * FROM MYLIB.MYTABLE");
<br>
// Create the SQLResultSetTableModel object.
SQLResultSetTableModel model = new SQLResultSetTableModel(rs);
<br>
// Create the enclosing JTable and put it in a JFrame.
JTable table = new JTable(model);
JFrame frame = new JFrame("My Window");
frame.getContentPane().add(new JScrollPane(table));
<br>
// Set up the error dialog adapter.
model.addErrorListener(new ErrorDialogAdapter(frame));
<br>
// Display the JFrame.
frame.pack();
frame.show();
<br>
// Load the contents of the model.
model.load();
</pre>
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
//
// Implementation notes:
//
// *  Note that this class throws error and working events from within
//    synchronized blocks, which could cause hangs if the listeners for
//    these events do operations from a different thread an attempt to
//    access another synchronized piece of code.
//
//    At this time this seems to be an acceptable risk, since the
//    events thrown are not likely to need enough processing to
//    require another thread, and getting having the events thrown
//    from outside a sychronized block would be nearly impossible.
//    The other option is to have the firing of the events be done
//    from another thread, but the overhead of creating another thread
//    not only takes resources, but also delays the delivery of the event.
//
// *  We do two sets of synchronization.  The internalMonitor_
//    synchronization prevents data corrruption when multiple threads
//    are accessing the same SQLResultSetTableModel object.
//    The resultSet_ synchronization prevents data corruption when
//    multiple SQLResultSetTableModels are accessing the same
//    ResultSet object.
//
// *  The variables which have private commented out had to made
//    package scope since currently Internet Explorer does not
//    allow inner class to access private variables in their
//    containing class.
//
public class SQLResultSetTableModel
    extends AbstractTableModel
    implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



  // Private data.
  private boolean                         cacheAll_               = false;
  private SQLConnection                   sqlConnection_          = null;
  private String                          query_                  = null;
  private ResultSet                       explicitResultSet_      = null;

  private transient Vector                cachedRows_;
  private transient int                   cachedRowCount_;
  private transient int                   columnCount_;
  private transient boolean               error_;
  private transient int                   firstCachedRow_;
  private transient Object                internalMonitor_;
  private transient int                   lastCachedRow_;
  private transient ResultSet             resultSet_;
  private transient ResultSetMetaData     resultSetMetaData_;
  private transient int                   rowCount_;
  private transient boolean               rowCountComplete_;
  private transient boolean               scrollable_;
  private transient Statement             statement_;
  private transient boolean               updatable_;

  private transient JTable                table_;    /* Keep a reference to the table so we can maintain selection information @B6A*/ 
  private static final int                CACHE_SIZE_             = 500;  // In rows.
  private static final int                READ_INCREMENT_         = 50;   // In rows.



  // Event support.
  private transient PropertyChangeSupport     propertyChangeSupport_;
  private transient VetoableChangeSupport     vetoableChangeSupport_;
  private transient ErrorEventSupport         errorEventSupport_;
  private transient WorkingEventSupport       workingEventSupport_;



  /**
  Constructs a SQLResultSetTableModel object.
  **/
  public SQLResultSetTableModel()
  {
    super();
    initializeTransient();
  }



  /**
  Constructs a SQLResultSetTableModel object.
  
  @param connection     The SQL connection.
  @param query          The SQL query.
  **/
  public SQLResultSetTableModel(SQLConnection connection, String query)
  {
    super();
    if(connection == null)
      throw new NullPointerException("connection");
    if(query == null)
      throw new NullPointerException("query");

    sqlConnection_ = connection;
    query_ = query;

    initializeTransient();
  }



  // @D0A
  /**
  Constructs a SQLResultSetTableModel object.
  
  @param resultSet  The SQL result set.
  @param cacheAll   true to cache the entire result set when <a href="#load()">load()</a>
                    is called, false to cache parts of the result set as they are
                    needed.  Passing true may result in slow initial presentation of
                    the data.  However, it may be necessary to pass true if the result
                    set is expected to close when the model is still needed.
  **/
  public SQLResultSetTableModel(ResultSet resultSet, boolean cacheAll)
  {
    super();
    if(resultSet == null)
      throw new NullPointerException("resultSet");

    explicitResultSet_ = resultSet;
    cacheAll_ = cacheAll;

    initializeTransient();
  }



  /**
  Adds a listener to be notified when an error occurs.
  
  @param  listener    The listener.
  **/
  public void addErrorListener(ErrorListener listener)
  {
    errorEventSupport_.addErrorListener(listener);
  }



  /**
  Adds a listener to be notified when the value of any
  bound property changes.
  
  @param  listener  The listener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChangeSupport_.addPropertyChangeListener(listener);
  }



  /**
  Adds a listener to be notified when the value of any
  constrained property changes.
  
  @param  listener  The listener.
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    vetoableChangeSupport_.addVetoableChangeListener(listener);
  }



  /**
  Adds a listener to be notified when work starts and stops
  on potentially long-running operations.
  
  @param  listener  The listener.
  **/
  public void addWorkingListener(WorkingListener listener)
  {
    workingEventSupport_.addWorkingListener(listener);
  }



  /**
  Clears all SQL warnings.
  **/
  public void clearWarnings()
  {
    try
    {
      if(resultSet_ != null)
        resultSet_.clearWarnings();
      if(statement_ != null)
        statement_.clearWarnings();
    }
    catch(SQLException e)
    {
      markError(e);
    }
  }



  /**
  Closes the result set.
  **/
  public void close()
  {
    try
    {
      if(resultSet_ != null)
      {
        resultSet_.close();
        resultSet_ = null;
      }
    }
    catch(SQLException e)
    {
      markError(e);
    }

    try
    {
      if(statement_ != null)
      {
        statement_.close();
        statement_ = null;
      }
    }
    catch(SQLException e)
    {
      markError(e);
    }
  }



  /**
  Returns the class of the values in the column.
  
  @param columnIndex The column index (0-based).
  @return            The class of the column values, or null
                     if an error occurs.
  **/
  public Class getColumnClass(int columnIndex)
  {
    // Returning Object seems to be sufficient.
    return Object.class;
  }



  /**
  Returns the number of columns in the table.
  
  @return The number of columns in the table, or 0
          if an error occurs.
  **/
  public int getColumnCount()
  {
    synchronized(internalMonitor_)
    {
      Trace.log(Trace.DIAGNOSTIC, "SQLResultSetTableModel.getColumnCount() = " + columnCount_);
      return columnCount_;
    }
  }



  /**
  Returns the column identifier.  This corresponds to the
  field name in the database.
  
  @param columnIndex  The column index (0-based).
  @return             The column identifier, or null
                      if an error occurs.
  **/
  public String getColumnID(int columnIndex)
  {
    synchronized(internalMonitor_)
    {
      if((columnIndex < 0) || (columnIndex >= columnCount_))
        return null;
      if(resultSet_ == null)
        return null;

      try
      {
        return resultSetMetaData_.getColumnName(columnIndex+1);
      }
      catch(SQLException e)
      {
        markError(e);
        return null;
      }
    }
  }



  /**
  Returns the column name.  This corresponds to the column
  label in the database.
  
  @param columnIndex  The column index (0-based).
  @return             The column name, or null
                      if an error occurs.
  **/
  public String getColumnName(int columnIndex)
  {
    synchronized(internalMonitor_)
    {
      if((columnIndex < 0) || (columnIndex >= columnCount_))
        return null;
      if(resultSet_ == null)
        return null;

      try
      {
        String col = resultSetMetaData_.getColumnLabel(columnIndex+1);        //@pdc extended metadata
        //columnLabel is a concatonation of up to three columns (each 20 length) with the 20 length padded with spaces
        int colLength = col.length();          //@pda extended metadata
        if( colLength > 20)                    //@pda  
        {                                      //@pda  
            
            if( colLength > 40)                        //@pda  
            {                                          //@pda  
                //contains three column concats
                String space1 = col.substring(19,20).equals(" ") ? " " : "";  //column separator
                String space2 = col.substring(39,40).equals(" ") ? " " : "";  //column separator
                col = col.substring(0, 20).trim() + space1 + col.substring(20, 40).trim() + space2 + col.substring(40).trim();    //@pda  
            }                                          //@pda  
            else                                       //@pda  
            {                                          //@pda  
                //contains two column concats
                String space1 = col.substring(19,20).equals(" ") ? " " : "";  //column separator
                col = col.substring(0, 20).trim() + space1 + col.substring(20).trim();  //@pda  
            }                                         //@pda  
        }                                             //@pda  
        return col;                                   //@pda  
      }
      catch(SQLException e)
      {
        markError(e);
        return null;
      }
    }
  }



  /**
  Returns the column type.
  
  @param columnIndex  The column index (0-based).
  @return             The column type, or 0 if an error occurs.
  **/
  public int getColumnType(int columnIndex)
  {
    synchronized(internalMonitor_)
    {
      if((columnIndex < 0) || (columnIndex >= columnCount_))
        return 0;
      if(resultSet_ == null)
        return 0;

      try
      {
        return resultSetMetaData_.getColumnType(columnIndex+1);
      }
      catch(SQLException e)
      {
        markError(e);
        return 0;
      }
    }
  }



  /**
  Returns the column width.
  
  @param columnIndex  The column index (0-based).
  @return             The column width, in characters, or 0 if an error occurs.
  **/
  public int getColumnWidth(int columnIndex)
  {
    synchronized(internalMonitor_)
    {
      if((columnIndex < 0) || (columnIndex >= columnCount_))
        return 0;
      if(resultSet_ == null)
        return 0;

      try
      {
        /* @D3D
        // For most types, we just consult with JDBC.  However, for times, dates, and
        // timestamps, JDBC reports the display size for the server format.  And these
        // GUIs actually internationalize the Strings before displaying them.  So for
        // these types, we need to compute our own display sizes.
        switch(resultSetMetaData_.getColumnType(columnIndex+1)) {
        case Types.TIME:
            return DBDateCellRenderer.getDisplaySize(DBDateCellRenderer.FORMAT_TIME);
        case Types.DATE:
            return DBDateCellRenderer.getDisplaySize(DBDateCellRenderer.FORMAT_DATE);
        case Types.TIMESTAMP:
            return DBDateCellRenderer.getDisplaySize(DBDateCellRenderer.FORMAT_TIMESTAMP);
        default:
        */
        return Math.min(resultSetMetaData_.getColumnDisplaySize(columnIndex+1), 50);
        // @D3D }
      }
      catch(SQLException e)
      {
        markError(e);
        return 0;
      }
    }
  }



  /**
  Returns the SQL connection.
  
  @return The SQL connection.
  **/
  public SQLConnection getConnection()
  {
    return sqlConnection_;
  }



  /**
  Returns the SQL query.
  
  @return The SQL query.
  **/
  public String getQuery()
  {
    return(query_ == null) ? "" : query_;
  }



  // @D0A
  /**
  Returns the SQL result set.
  
  @return The SQL result set.
  **/
  public ResultSet getResultSet()
  {
    return explicitResultSet_;
  }



  /**
  Returns the number of rows in the table.
  Because of incremental data retrieval, this value may
  not be accurate.
  
  @return The number of rows in the table.
  **/
  public int getRowCount()
  {
    if(resultSet_ == null)
      return 0;

    // If we are not complete, report 2 more than actually here.
    // This will trick JTable into continuing to ask for more.
    int reportedRowCount = rowCount_;
    if(!rowCountComplete_)
      reportedRowCount += 2;

    // This should not be in a synchronized block, otherwise
    // a hang may occur.
    Trace.log(Trace.DIAGNOSTIC, "SQLResultSetTableModel.getRowCount() = " + reportedRowCount + "(actually " + rowCount_ + ")");
    return reportedRowCount;
  }



  // @D4A
  private Object getSingleValue(int columnIndex) throws SQLException
  {
    int type = resultSetMetaData_.getColumnType(columnIndex);
    if(type == Types.BINARY
       || type == Types.VARBINARY
       || type == Types.LONGVARBINARY)
    {
      return resultSet_.getBytes(columnIndex);
    }
    else
    {
      // Use getString() so that dates and times get converted to strings
      // by the JDBC driver so that their formats reflect the settings
      // in the data source.
      //@KKB return resultSet_.getString(columnIndex);
      String s = resultSet_.getString(columnIndex);     //@KKB
      if(checkDataMappingWarning(resultSet_, columnIndex))           //@KKB
          s="++++++++++++++";                           //@KKB
      return s;                                         //@KKB
    }
  }



  /**
  Returns the value at the specifed row and column.
  
  @param rowIndex       The row index (0-based).
  @param columnIndex    The column index (0-based).
  
  @return The value at the specified row and column.
  **/
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    int oldRowCount;
    int newRowCount;
    Object[] row = null;

    synchronized(internalMonitor_)
    {
      if((columnIndex < 0) || (columnIndex >= columnCount_))
        return null;
      if(rowIndex < 0)
        return null;
      if((rowCountComplete_) && (rowIndex >= rowCount_))
        return null;
      if(resultSet_ == null)
        return null;

      oldRowCount = rowCount_;

      // Case 1: If this row is in the cache, just read it from there.
      // If cacheAll_ is set, then this case should always occur.
      if((rowIndex >= firstCachedRow_) && (rowIndex <= lastCachedRow_))
      {
        row = (Object[])cachedRows_.elementAt(rowIndex - firstCachedRow_);
      }

      // Case 2: If this row comes before whats in the cache, read the rows
      // and move the cache window up in the result set.  This will only
      // occur if the result set is scrollable - if it is not, then the
      // cache always includes everything read so far.
      else if((rowIndex < firstCachedRow_) && (error_ == false))
      {
        workingEventSupport_.fireStartWorking();
        for(int i = firstCachedRow_ - 1; i >= rowIndex; --i)
        {
          try
          {
            row = new Object[columnCount_];
            synchronized(resultSet_)
            {

              // If we are just reading the next row since the last
              // time, it can be significantly faster to call next().
              // This is especially true when using the Toolbox
              // JDBC driver since it does record blocking only
              // when using next().
              //
              // I tried keeping the last read row locally, but it
              // messes things up when result sets are shared
              // between multiple models.
              //
              if(i == resultSet_.getRow())
                resultSet_.next();
              else
                resultSet_.absolute(i+1);

              // Store the contents of the row.
              for(int j = 0; j < columnCount_; ++j)
                row[j] = getSingleValue(j+1); // @D4C
            }
            cachedRows_.insertElementAt(row, 0);
            --firstCachedRow_;
            if(++cachedRowCount_ > CACHE_SIZE_)
            {
              cachedRows_.removeElementAt(--cachedRowCount_);
              --lastCachedRow_;
            }
          }
          catch(SQLException e)
          {
            markError(e);
          }
        }
        workingEventSupport_.fireStopWorking();
      }

      // Case 3: If this row comes after whats in the cache, read the rows
      // and move the cache window down in the result set.  If we do not
      // know the final row count, then jump a little bit ahead to discover
      // this.
      else if((rowIndex > lastCachedRow_) && (error_ == false))
      {

        // Determine how far to go.  If we are not complete yet, push it
        // ahead to force the table to read even further.
        int endPoint = rowIndex;
        if(!rowCountComplete_)
          endPoint = rowIndex + READ_INCREMENT_;

        // Loop through the rows, quit if we get to the end.
        workingEventSupport_.fireStartWorking();
        boolean valid = true;
        for(int i = lastCachedRow_ + 1; (i <= endPoint) && (valid); ++i)
        {
          try
          {
            synchronized(resultSet_)
            {

              // If we are just reading the next row since the last
              // time, it can be significantly faster to call next().
              // This is especially true when using the Toolbox
              // JDBC driver since it does record blocking only
              // when using next().   We also take this route if
              // the result set is not scrollable.
              if((i == resultSet_.getRow()) || (!scrollable_))
                valid = resultSet_.next();
              else
                valid = resultSet_.absolute(i+1);

              // If this is a valid row, then add it to the cache.
              if(valid)
              {
                Object[] tempRow = new Object[columnCount_];
                for(int j = 0; j < columnCount_; ++j)
                  tempRow[j] = getSingleValue(j+1); // @D4C
                if(i == rowIndex)
                  row = tempRow;
                cachedRows_.insertElementAt(tempRow, cachedRowCount_++);
                ++lastCachedRow_;
                if((scrollable_) && (cachedRowCount_ > CACHE_SIZE_))
                {
                  cachedRows_.removeElementAt(0);
                  cachedRowCount_--;
                  ++firstCachedRow_;
                }

                // If this is greater than our current count, add record it.
                if((!rowCountComplete_) && (i >= rowCount_))
                  rowCount_ = i;
              }

              // If this is not a valid row, then mark the row count as
              // complete.
              else if(!rowCountComplete_)
              {
                rowCountComplete_ = true;
                rowCount_ = i;
              }
            }
          }
          catch(SQLException e)
          {
            markError(e);
          }
        }
        workingEventSupport_.fireStopWorking();
      }

      // If the row count changed, record the change so we can fire the table
      // model event (outside of the synchronized block).
      newRowCount = rowCount_;
    }

    // If the row count changed, fire the table model event.  If we are
    // not complete, then add 2 to keep the JTable asking for more.
    if(oldRowCount != newRowCount)
    {
      // In JDK 1.5 and 1.6, there is a problem with the selection in the JTable being cleared. 
      // This causes problems if the user is using the page down key to scroll through the results.
      //
      // The call to fireTableRowsInserted may be the code that is clearing the selection.  
      // For a similar problem see:  http://stackoverflow.com/questions/254212/preserve-jtable-selection-across-tablemodel-change
      // I tried setting the first parameter to oldRowCount+1 to see if that fixes the problem.	
      // That did not fix the problem.  The row at the bottom retains it's highlight, but the next
      // PageDown jumps to the top of the area. 
      // 
      // Instead we need to remember the selected rows and restore them after. 
      // For now, we'll only handle the simple case where a single cell is selected. 
      //
      // We also only want to change the selection if the selected row has changed after  
      // fireTableRowsInserted.  In the case where the use of the scroll bar causes new
      // entries to be loaded, the selectedRow does not change, so we do not need to 
      // change the selection back to their original values. 
      //
      // @B6A 
      
      
      int  selectedRow = -1; 
      if (table_ != null)   selectedRow = table_.getSelectedRow();
      int selectedColumn = -1; 
      if (table_ != null)   selectedColumn = table_.getSelectedColumn(); 
      
      fireTableRowsInserted(oldRowCount, getRowCount());
      
      
      
      // Restore the selected cell.  @B6A 
     int  afterSelectedRow = -1 ; 
     if (table_ != null)	afterSelectedRow = table_.getSelectedRow();
      if ((selectedRow >= 0 && selectedColumn >= 0 ) && 
    		  ((afterSelectedRow != selectedRow) )) {
      table_.changeSelection(selectedRow, selectedColumn, false /* toggle */ , false /* extend */); 
	  
      }
    }

    // Return the value.
    if(row == null)
      return null;
    else
      return row[columnIndex];
  }



  /**
  Returns the SQL warnings.
  
  @return The SQL warnings.
  **/
  public SQLWarning getWarnings()
  {
    SQLWarning warnings = null;
    try
    {
      if(resultSet_ != null)
        warnings = resultSet_.getWarnings();
      if(statement_ != null)
      {
        if(warnings == null)
          warnings = statement_.getWarnings();
        else
          warnings.setNextWarning(statement_.getWarnings());
      }
    }
    catch(SQLException e)
    {
      markError(e);
    }
    return warnings;
  }



  /**
  Initializes the common data.
  **/
  private void initializeCommon()
  {
    synchronized(internalMonitor_)
    {
      cachedRows_             = new Vector(CACHE_SIZE_);
      cachedRowCount_         = 0;
      columnCount_            = 0;
      error_                  = false;
      firstCachedRow_         = 0;
      lastCachedRow_          = -1;
      rowCount_               = 0;
      rowCountComplete_       = false;
    }
  }



  /**
  Initializes the transient data.
  **/
  private void initializeTransient()
  {
    internalMonitor_                = new Object();

    initializeCommon();

    // Event support.
    propertyChangeSupport_          = new PropertyChangeSupport(this);
    vetoableChangeSupport_          = new VetoableChangeSupport(this);
    errorEventSupport_              = new ErrorEventSupport(this);
    workingEventSupport_            = new WorkingEventSupport(this);
  }


  //@D6A
  /**
   * Used by SQLResultSetTablePane to determine whether or not the default
   * cell editor should be replaced.
  **/
  boolean isUpdatable()
  {
    return updatable_;
  }


  // @D2A
  /**
  Indicates if the cell is editable.
  
  @param rowIndex         The row index (0-based).
  @param columnIndex      The column index (0-based).
  @return                 true if the cell is editable, false otherwise
                          or if an error occurs.
  **/
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    synchronized(internalMonitor_)
    {
      if((columnIndex < 0) || (columnIndex >= columnCount_))
        return false;
      if(rowIndex < 0)
        return false;
      if((rowCountComplete_) && (rowIndex >= rowCount_))
        return false;
      if(resultSet_ == null)
        return false;

      //@D6D            try {
      //@D6D                return (updatable_ && resultSetMetaData_.isWritable(columnIndex+1));
      //@D6D            }
      //@D6D            catch(SQLException e) {
      //@D6D                markError(e);
      //@D6D                return false;
      //@D6D            }
      return true; //@D6A
    }
  }



  /**
  Loads the data in the table.
  **/
  public void load()
  {
    Trace.log(Trace.DIAGNOSTIC, "SQLResultSetTableModel.load()");
    workingEventSupport_.fireStartWorking();

    // Make sure we have enough data to load.
    if(explicitResultSet_ == null)
    {
      if(sqlConnection_ == null)
        throw new IllegalStateException("connection");
      if(query_ == null)
        throw new IllegalStateException("query");
    }

    synchronized(internalMonitor_)
    {

      // Clear the old data.
      initializeCommon();

      // Load the new data.
      try
      {
        if(explicitResultSet_ == null)
        {
          if(statement_ == null)
          {
            Connection connection = sqlConnection_.getConnection();
            statement_ = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
          }

          resultSet_ = statement_.executeQuery(query_);
        }
        else
          resultSet_ = explicitResultSet_;

        scrollable_ = (resultSet_.getType() != ResultSet.TYPE_FORWARD_ONLY);
        updatable_ = (resultSet_.getConcurrency() == ResultSet.CONCUR_UPDATABLE);  // @D2A
        resultSetMetaData_ = resultSet_.getMetaData();
        columnCount_ = resultSetMetaData_.getColumnCount();

        if(Trace.isTraceOn())
        {
          Trace.log(Trace.DIAGNOSTIC, "SQLResultSetTableModel-scrollable? " + scrollable_);
          Trace.log(Trace.DIAGNOSTIC, "SQLResultSetTableModel-updatable? " + updatable_); // @D2A
        }

        //@KBD removed since a stored procedure may or may not return a scrollable cursor
        // if the user specified ResultSet.TYPE_SCROLL_SENSITIVE or ResultSet.TYPE_SCROLL_INSENSITVE
        //@KBD if(scrollable_)
        //@KBD {
        //@KBD     resultSet_.beforeFirst();
        //@KBD }
      }
      catch(SQLException e)
      {
        markError(e);
        rowCountComplete_ = true;
      }

      // If we are supposed to, then cache all.
      if(cacheAll_)
      {
        try
        {
          while(resultSet_.next())
          {
            Object[] tempRow = new Object[columnCount_];
            for(int j = 0; j < columnCount_; ++j)
              tempRow[j] = getSingleValue(j+1); // @D4C
            cachedRows_.addElement(tempRow);
          }
        }
        catch(SQLException e)
        {
          markError(e);
        }
        lastCachedRow_ = cachedRows_.size();
        rowCount_ = lastCachedRow_;
        rowCountComplete_ = true;
      }

      // Otherwise, try to read 1 row.  This will handle the case where there are no rows.
      else
      {
        getValueAt(0, 0);
      }
    }

    // Tell the JTable that we've changed.
    fireTableStructureChanged();

    workingEventSupport_.fireStopWorking();
  }



  /**
  Marks that an error has occurred.
  **/
  private void markError(Exception e)
  {
    if(Trace.isTraceOn())
      Trace.log(Trace.ERROR, "Error gathering SQLResultSetTableModel data", e);

    // Only fire if this is the first error.  That way, we don't
    // barrage the poor user.
    if(!error_)
    {
      error_ = true;
      errorEventSupport_.fireError(e);
    }
  }



  /**
  Restores the state of the object from an input stream.
  This is used when deserializing an object.
  
  @param in   The input stream.
  **/
  private void readObject(ObjectInputStream in)
  throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    initializeTransient();
  }



  /**
  Removes an error listener.
  
  @param  listener    The listener.
  **/
  public void removeErrorListener(ErrorListener listener)
  {
    errorEventSupport_.removeErrorListener(listener);
  }



  /**
  Removes a property change listener.
  
  @param  listener  The listener.
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChangeSupport_.removePropertyChangeListener(listener);
  }



  /**
  Removes a vetoable change listener.
  
  @param  listener  The listener.
  **/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    vetoableChangeSupport_.removeVetoableChangeListener(listener);
  }                                                    



  /**
  Removes a working listener.
  
  @param  listener  The listener.
  **/
  public void removeWorkingListener(WorkingListener listener)
  {
    workingEventSupport_.removeWorkingListener(listener);
  }



  /**
  Sets the SQL connection.
  The data will not change until <a href="#load()">load()</a> is called.
  
  @param       connection              The SQL connection.
  
  @exception  PropertyVetoException   If the change is vetoed.
  **/
  public void setConnection(SQLConnection connection)
  throws PropertyVetoException
  {
    if(connection == null)
      throw new NullPointerException("connection");

    SQLConnection oldValue = sqlConnection_;
    vetoableChangeSupport_.fireVetoableChange("connection", oldValue, connection);
    sqlConnection_ = connection;
    statement_ = null;
    propertyChangeSupport_.firePropertyChange("connection", oldValue, connection);
  }



  /**
  Sets the SQL query.
  The data will not change until <a href="#load()">load()</a> is called.
  
  @param       query                   The SQL query.
  
  @exception  PropertyVetoException   If the change is vetoed.
  **/
  public void setQuery(String query)
  throws PropertyVetoException
  {
    if(query == null)
      throw new NullPointerException("query");

    String oldValue = query_;
    vetoableChangeSupport_.fireVetoableChange("query", oldValue, query);
    query_ = query;
    propertyChangeSupport_.firePropertyChange("query", oldValue, query);
  }



  // @D0A
  /**
  Sets the SQL result set used to build the table.  If this is set,
  it is used instead of the SQL connection and SQL query.
  The data will not change until <a href="#load()">load()</a> is called.
  
  @param resultSet        The SQL result set.
  **/
  public void setResultSet(ResultSet resultSet)
  {
    if(resultSet == null)
      throw new NullPointerException("resultSet");

    ResultSet oldValue = explicitResultSet_;
    explicitResultSet_ = resultSet;
    propertyChangeSupport_.firePropertyChange("resultSet", oldValue, resultSet);
  }



  // @D2A
  /**
  Sets the value at the specifed row and column.
  
  @param value          The value.
  @param rowIndex       The row index (0-based).
  @param columnIndex    The column index (0-based).
  **/
  public void setValueAt(Object value, int rowIndex, int columnIndex)
  {
    synchronized(internalMonitor_)
    {
      if((columnIndex < 0) || (columnIndex >= columnCount_))
        return;
      if(rowIndex < 0)
        return;
      if((rowCountComplete_) && (rowIndex >= rowCount_))
        return;
      if(resultSet_ == null)
        return;

      if(!updatable_) return; //@D6A

      if(scrollable_)
      {
        try
        {
          // Update the value in the result set.
          synchronized(resultSet_)
          {
            resultSet_.absolute(rowIndex+1);
            resultSet_.updateObject(columnIndex+1, value);
            resultSet_.updateRow();
          }

          // Update the value in the cache.
          ((Object[])cachedRows_.elementAt(rowIndex - firstCachedRow_))[columnIndex] = value;
        }
        catch(SQLException e)
        {
          // Don't set error_ to true, because that will ruin the whole
          // model.  Just fire an event and do not update the cache.
          errorEventSupport_.fireError(e);
        }

      }
    }
  }

  //@KKB - Checks if a Data Mapping Warning was issued
  private boolean checkDataMappingWarning(ResultSet rs, int columnIndex) throws SQLException{
      boolean dataMapping = false;
      SQLWarning w = rs.getWarnings();
      if(w!=null){
          do{
              if(w.getSQLState().equals("01004") && ((java.sql.DataTruncation)w).getDataSize() == -1 && ((java.sql.DataTruncation)w).getTransferSize() == -1 && ((java.sql.DataTruncation)w).getIndex() == columnIndex)
                  dataMapping = true;

              w=w.getNextWarning();
          }while(w!=null);
      }

      return dataMapping;
  }

  
  
/**
 * Set a reference to the JTable so that the selected row can be updated after 
 * fireTableRowsInserted. 
 * 
 * @param table
 */
public void setTable(JTable table) {
	table_ = table; 
	
}


  
  
}



