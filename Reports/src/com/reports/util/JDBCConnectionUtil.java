package com.reports.util;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.mysql.jdbc.ResultSetMetaData;
import com.reports.service.cAttendanceDetailsService;

/**
 * 
 */

/**
 * @author 70072
 *
 */
public class JDBCConnectionUtil {

	public static Connection JDBCConnection = getJDBCConnection();
	
	static final String ATTENDANCE_FILE = "EDS_Attendance";
	
	//static final String ATTENDANCE_FILE = "Ranal_Attendance";
	
    public static Connection getJDBCConnection()
    {

    	 getAbsolutePath();
         String lServerName = null;
         String lPortNumber = null;
         String lDatabaseName = null;
         String lInstanceName = null;
         String lUserName = null;
         String lPassword = null;
         String lDatabase = null;
         String lProtocol = null;
         try
            {
                 PropertyResourceBundle lCustomerProperties = (PropertyResourceBundle)ResourceBundle.getBundle(ATTENDANCE_FILE);

                 // For the location get the Site Number

                 lServerName            = lCustomerProperties.getString("serverName_RESI");
                 lPortNumber            = lCustomerProperties.getString("Port_RESI");
                 lDatabaseName          = lCustomerProperties.getString("DatabaseName_RESI");
                 lInstanceName          = lCustomerProperties.getString("InstanceName_RESI");
                 lUserName               = lCustomerProperties.getString("UserName_RESI");
                 lPassword              = lCustomerProperties.getString("Password_RESI");
                 if(lCustomerProperties.handleGetObject("Database")!=null)
                	 lDatabase             = lCustomerProperties.getString("Database");
                 lProtocol              = lCustomerProperties.getString("Protocol_RESI");
          }

          catch (Exception e1)
          {
                e1.printStackTrace();
          }

        Connection aConnection = null;

         if(lDatabase == null || lDatabase.trim().equalsIgnoreCase("mysql") || lDatabase.trim().equalsIgnoreCase(""))
         {
             String lConnectionString = "jdbc:mysql://"+lServerName+":"+lPortNumber+"/"+lDatabaseName;
             try
             {
                 Class.forName("com.mysql.jdbc.Driver");
                 aConnection = java.sql.DriverManager.getConnection(lConnectionString,lUserName,lPassword);
             }
             catch (ClassNotFoundException e)
             {
                 e.printStackTrace();
             }
             catch (Exception pE)
             {
                 pE.printStackTrace();
             }
         }
         else if(lDatabase != null && lDatabase.trim().equalsIgnoreCase("sqlserver"))
         {
              if(!lProtocol.equals("TCP"))
              {
                 String lConnectionString = "jdbc:jtds:sqlserver://"+lServerName+":"+lPortNumber+"/"+lDatabaseName+";instance="+lInstanceName;
                 try
                 {
                	 Class.forName("net.sourceforge.jtds.jdbc.Driver");
                 }
                 catch (ClassNotFoundException e)
                 {
                    e.printStackTrace();
                 }
                 try {
					aConnection = java.sql.DriverManager.getConnection(lConnectionString,lUserName,lPassword);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                 }
                 else
                 {
                     String lConnectionString1 = "jdbc:sqlserver://"+lServerName+";databasename="+lDatabaseName+";instanceName="+lInstanceName;
                     try
                     {
                    	 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                     }
                     catch (ClassNotFoundException e)
                     {
                        e.printStackTrace();
                     }
                     try {
						aConnection = java.sql.DriverManager.getConnection(lConnectionString1,lUserName,lPassword);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                 }
         }
         
        return aConnection;
    }
    
	
 	public static void closeConnection(Connection connection) {
	     try
	     {
	          if(connection!=null)
	        	  connection.close();
	          connection=null;
	     }
	     catch(Exception e)
	     {
	          e.printStackTrace();
	     }
	}
 	
 	public static void mCloseStmt(Statement lStmt) {
		if(lStmt!=null) {
			try {
				lStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
 	public static void descAttendanceDetails() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "exec sp_columns EDS_DailyAttendance";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getAttendanceDetails() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "SELECT UserName, UserID, processdate, worktime FROM EDS_DailyAttendance where UserName like 'Sat%'";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString("UserName")+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(4)+" "+rs.getString("processdate")+" "+rs.getString("worktime"));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void getAttendanceDetailsJoin() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			Date toDate = new Date();
			cal.set(2015, 07, 01);
			Date fromDate = cal.getTime();
			System.out.println(fromDate+"="+toDate);
			String fromDateStr = dateFormat.format(fromDate);
			String toDateStr = dateFormat.format(toDate);
			String query = "SELECT ad.UserID, em.employeename, ad.EntryExit, ad.DoorControllerID, ad.DateTime_d FROM AttendanceDetails as ad, employeemaster as em WHERE ad.UserID=em.employeecode AND ad.EntryExit=1 AND (DateTime_d BETWEEN '"+fromDateStr+"' AND '"+toDateStr+"') group by ad.EntryExit, ad.UserID, DATE(ad.DateTime_d)";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			String refUserId = "";
			int reportedDays = 0;
			while(rs.next()) {
				if(refUserId.equals("")){
					refUserId = rs.getString(1);
				}
				reportedDays++;
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+"--"+reportedDays);
				if(!rs.getString(1).equals(refUserId)){
					
					reportedDays = 0;
					refUserId = rs.getString(1);
				}
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void descUserMasterData() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "exec sp_columns EDS_UserMasterData";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static Date mGetAllUsersDateOfJoin() {
		Connection conn = JDBCConnection;
		Statement lStmt = null;
		Date lDateOfJoin = null;
		String lUserID = null;

		try {			
			String lQuery = "SELECT UserID, JoinDT FROM EDS_UserMasterData group by UserID, JoinDT";
			
			lStmt = conn.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			
			while(rs.next()) {
				lUserID = rs.getString(1);	
				lDateOfJoin = rs.getDate(2);
				
				System.out.println(lUserID + " : " + lDateOfJoin);
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(lStmt!=null)
				try {
					lStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return lDateOfJoin;
	}
	public static void descLeaveData() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "exec sp_columns EDS_LeaveData";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void descACSInOutSwipes() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "exec sp_columns EDS_ACSInOutSwipes";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static Date mGetACSInOutSwipes() {
		Connection conn = JDBCConnection;
		Statement lStmt = null;
		Date lDateOfJoin = null;
		String lUserID = null;

		try {			
			String lQuery = "SELECT * FROM EDS_ACSInOutSwipes";
			
			lStmt = conn.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			
			while(rs.next()) {
				lUserID = rs.getString(1);	
				lDateOfJoin = rs.getDate(2);
				
				System.out.println(rs.getString(1) + " : " + rs.getTime(2) + " " + rs.getString(3) + " " + rs.getString(4)+ " ----- " + rs.getString(5)+ " ----- " + rs.getString(6));
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(lStmt!=null)
				try {
					lStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return lDateOfJoin;
	}
	
	public static void mGetTAInOutSwipes() {
		Connection conn = JDBCConnection;
		Statement lStmt = null;
		Date lDateOfJoin = null;
		String lUserID = null;

		try {			
			String lQuery = "SELECT * FROM EDS_TAInOutSwipes where UserId = '91780' order by Edatetime";
			
			//String lQuery = "SELECT * FROM EDS_RawAttendanceEvents where UserId = '91984' and  (EventDateTime between '2016-01-08' and '2016-02-24 23:59:59.999') order by EventDateTime";
			//String lQuery = "SELECT * FROM EDS_RawAttendanceEvents where UserId = '91814' and  (EventDateTime between '2016-01-08' and '2016-02-24 23:59:59.999') order by EventDateTime";
			//String lQuery = "SELECT distinct DoorControllerID FROM EDS_RawAttendanceEvents";
			
			/*MATRIXTA dbo EDS_RawAttendanceEvents EventDateTime
			MATRIXTA dbo EDS_RawAttendanceEvents EntryExitType
			MATRIXTA dbo EDS_RawAttendanceEvents MasterControllerID
			MATRIXTA dbo EDS_RawAttendanceEvents DoorControllerID
			MATRIXTA dbo EDS_RawAttendanceEvents SpecialFunctionID
			MATRIXTA dbo EDS_RawAttendanceEvents EventID
			MATRIXTA dbo EDS_RawAttendanceEvents Panel_Door_Type
			MATRIXTA dbo EDS_RawAttendanceEvents Edate
			MATRIXTA dbo EDS_RawAttendanceEvents ETime
			MATRIXTA dbo EDS_RawAttendanceEvents IDateTime
			MATRIXTA dbo EDS_RawAttendanceEvents SiteID
			*/
			lStmt = conn.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			
			while(rs.next()) {
				//lUserID = rs.getString(1);	
				//lDateOfJoin = rs.getDate(2);
				
				//System.out.println("=="+rs.getString(1) + " : " + rs.getDate(2) + " : " + rs.getTime(2) + " " + rs.getString(3) + " ----- " + rs.getString(5)+ " ----- " + rs.getString(6)+ " ----- ");
				//if(rs.getString(31).startsWith("2016-02-25"))
				//System.out.println("=="+rs.getString(34) + "=="+ rs.getString(31) + "=="+ rs.getString(32) + "=="+ rs.getString(35));
				System.out.println(rs.getString("Edatetime") + " - " + rs.getString("IOType") + " - " + rs.getString("DID")+ " - " + rs.getString("DoorName"));
				
			}
			
			
			String query = "exec sp_columns EDS_RawAttendanceEvents";
			lStmt = conn.createStatement();
			rs = lStmt.executeQuery(query);
			int i = 1;
			while(rs.next()) {
				System.out.println(i + " .."+ rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6));
				i++;
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(lStmt!=null)
				try {
					lStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}
	public static void mGetUniqueDoors() {
		Connection conn = JDBCConnection;
		Statement lStmt = null;
		Date lDateOfJoin = null;
		String lUserID = null;

		try {			
			String lQuery = "SELECT distinct swipes.UserID, master.Name,  swipes.MID, swipes.DoorName FROM EDS_TAInOutSwipes swipes right join EDS_UserMasterData master on swipes.UserID = master.UserID";
			
			lStmt = conn.createStatement();
			ResultSet rs = lStmt.executeQuery(lQuery);
			
			while(rs.next()) {
				System.out.println("User ID : " + rs.getString(1));
				System.out.println("Name : " + rs.getString(2));
				System.out.println("MID : " + rs.getString(3));
				System.out.println("Door Name" + rs.getString(4));
				
				System.out.println("=============================================");
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(lStmt!=null)
				try {
					lStmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}
	public static void descTAInOutSwipes() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "exec sp_columns EDS_TAInOutSwipes";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getLeaveData() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "select * from EDS_LeaveData where SNCNFlg != 0 and UserID='91826'";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+
								   rs.getString(2)+" "+
						           rs.getString(3)+" "+
								   rs.getString(4)+" "+
						           rs.getString(5)+" "+
								   rs.getString(6)+" "+ 
								   rs.getString(7)+" "+ 
								   rs.getString(8)+" "+ 
								   rs.getString(9)+" "+ 
								   rs.getString(10)+" "+ 
								   rs.getString(11)+" ---- " +
								   rs.getString("flgFinalApproval")
								   );
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void descATDCorrectionData() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "exec sp_columns EDS_ATDCorrection";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getATDCorrectionData() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "select * from EDS_ATDCorrection";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+
								   rs.getString(2)+" "+
						           rs.getString(3)+" "+
								   rs.getString(4)+" "+
						           rs.getString(5)+" "+
								   rs.getString(6)+" "+ 
								   rs.getString(7)+" "+ 
								   rs.getString(8)+" "+ 
								   rs.getString(9)+" "+ 
								   rs.getString(10)+" "+ 
								   rs.getString(11)+" ---- " +
								   rs.getString("flgFinalApproval")
								   );
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void descHolidaySchedule() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "exec sp_columns EDS_HolidaySchedule";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getHolidaySchedule() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			//String query = "SELECT * FROM EDS_HolidaySchedule WHERE HLDID = 1 AND CnfgdFlg = 1";
			String query = "SELECT CONCAT(HLDID, '&', YEAR(HLDDT),'-', MONTH(HLDDT)),  COUNT(*) FROM [cosec].[dbo].[EDS_HolidaySchedule] " +
						   " where HLDDT  > '2015-01-04' and HLDDT < '2015-07-07'" +
						   " GROUP BY CONCAT(HLDID, '&', YEAR(HLDDT),'-', MONTH(HLDDT))";
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getInt(2));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void getUserMasterData() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			//String query = "SELECT * FROM EDS_HolidaySchedule WHERE HLDID = 1 AND CnfgdFlg = 1";
			String query = "SELECT Name, UserID, SGID, JoinDT, LeaveDT FROM EDS_UserMasterData";
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" || "+rs.getString(2)+" || "+rs.getString(3)+" || "+rs.getString(4));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void getUserMaster() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "SELECT * FROM EDS_UserMasterData";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" === "+rs.getString("hldid"));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}


	public static void getDatabaseTables() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "SELECT * FROM INFORMATION_SCHEMA.TABLES";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getRawAttendanceEvents() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "SELECT * FROM EDS_RawAttendanceEvents where UserID='91977' order by Edate";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getDate("EventDateTime")+" "+rs.getTime("ETime")+" "+rs.getString("SpecialFunctionID")+" "+rs.getString("EntryExitType"));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}


	public static void descRawAttendanceEvents() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "exec sp_columns EDS_RawAttendanceEvents";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getWorktime() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "select processdate, worktime " +
					"FROM EDS_DailyAttendance where UserID = '70072'";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getDailyAttendance() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "select * FROM EDS_DailyAttendance where UserID='91780' AND processdate='14/01/2016'";
			
			//query = "select distinct ScheduleShift " +
			//		"FROM EDS_DailyAttendance";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while(rs.next()) {
				System.out.print(rs.getString("processdate") + " == " + rs.getString("UserID") + " == " + rs.getString("FirstHalf") + " == "+ rs.getString("PersOffReason") + " == ");
				for (int i = 1; i < 41; i++) {
					if (rs.getString(i) != null) {
						System.out.print(i + " - " + rs.getString(i) + " == ");
					}
					
				}
				System.out.println("============================");
				//System.out.println(rs.getString("FirstHalf") + " " + rs.getString("SecondHalf") + " " + rs.getString("UserID")+ " " + rs.getString("processdate")+ " " + rs.getString("OutPunch"));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getDailyStrength() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "select processdate, count(*) " +
					"FROM EDS_DailyAttendance group by processdate";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static void getOD() {
		Connection conn = JDBCConnection;
		Statement stmt = null;
		try {
			String query = "select processdate, 0.5 * count(*) " +
					"FROM EDS_DailyAttendance where UserID='70070' and (FirstHalf = 'OD' OR SecondHalf = 'OD') group by processdate";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				System.out.println(rs.getString(1)+" "+rs.getString(2));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public static void getAbsolutePath() {
		String path = new File("").getAbsolutePath();
		System.out.println(path);
		
	}
	public static void main(String[] args) {
		
		getAbsolutePath();
		//descACSInOutSwipes();
		//mGetACSInOutSwipes();
		//descTAInOutSwipes();
	    //mGetTAInOutSwipes();
		//getUserMaster();
		//descAttendanceDetails();
		//getDailyAttendance();
		//getAttendanceDetails();
		//getWorktime();
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 6, 1);
		//descUserMasterData();
		//mGetTAInOutSwipes();
		//getDatabaseTables();
		//descRawAttendanceEvents();
		//getRawAttendanceEvents();
		//getUserMasterData();
		//descHolidaySchedule();
		//descLeaveData();
		//mGetUniqueDoors();
		//getLeaveData();
		//getDatabaseTables();
		//descATDCorrectionData();
		//getATDCorrectionData();
		//getHolidaySchedule();
		//new AttendanceDetailsService().getUserMonthlyLeavesAvailed("70072", cal.getTime(), new Date());
		//new AttendanceDetailsService().getUserMonthlyPresence("70072", cal.getTime(), new Date());
	}

}
