package com.gaon.nifou.v3;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;



public class trans_ora_manager {
	private Properties prop = new Properties();
	private InputStream is = null;
	private Connection con  = null;
	private PreparedStatement stmt = null;
	private ResultSet rs = null;
	private StringBuffer strbuf = null;
	private ResultSetMetaData rsmd;
	private String status;
	private String jdbc_driver;
	private String db_url[] = null;
	private String db_id[] = null;
	private String db_pwd[] = null;
	private String debugmode = null;
	private util_manager um = new util_manager();
	
	//DB�뜝�룞�삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕
	public Connection getOraConnect(){
		Connection con = null;
		
		String url = null;
		String id = null;
		String pwd = null;
		try {
			is = trans_ora_manager.class.getClassLoader().getResourceAsStream("dbcon.properties");
	        prop.load(is);
	        status = prop.getProperty("status");
	        jdbc_driver = prop.getProperty("jdbc_driver");
	        db_url = prop.getProperty("url").split("&&");
	        db_id = prop.getProperty("id").split("&&");
	        db_pwd = prop.getProperty("pwd").split("&&");
	        debugmode = prop.getProperty("debugmode");
	        
	        if(this.status.equals("test")) {
				url = db_url[0];
				id = db_id[0];
				pwd = db_pwd[0];
			}else if(this.status.equals("real")) {
				url = db_url[1];
				id = db_id[1];
				pwd = db_pwd[1];
			}
	    } catch (Exception e) {
	        System.out.println("PROP LOAD ERROR : " + e.getMessage());
	    }
		try {
			//com.ibm.db2.jcc.DB2Driver
			//jdbc:db2://175.207.12.32:1521/ORCL
			//ifou
			//1
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@175.207.12.32:1521/ORCL", "ifou", "1");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}


	public void setOraClose(Connection con, PreparedStatement stmt, ResultSet rs){
		try{
			if (rs != null){
				rs.close();
				rs = null;
			}
		}catch (Exception e){e.printStackTrace();}

		try{
			if (stmt != null){
				stmt.close();
				stmt = null;
			}
		}catch (Exception e){e.printStackTrace();}
		
		try {
			if(con!=null) {con.close();}
		} catch (Exception e) {e.printStackTrace();}
		
		try {
			if(strbuf!=null) {strbuf = null;}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void rollBack(Connection con) {
		if(con!=null) 
		try{
		    con.rollback();
		}catch(SQLException sqle) {}
	}
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕 Select Query
	 * @param uid : �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�떛�벝�삕
	 * @return count(1)
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public int get_user_cnt(String uid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		int rtncnt = 0;
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT COUNT(1) MCNT FROM TB_BAS_USER WHERE USER_ID=?");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			stmt.setString(1, uid); //�뜝�룞�삕�뜝�룞�삕 ID
			rs = stmt.executeQuery();
			rs.next();

			rtncnt = rs.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			setOraClose(con,stmt,rs);
		}
		return rtncnt;
	}
	
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕 Select Query
	 * @param uid : �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�떛�벝�삕
	 * @return USER_PW, DEP_CD, ORG_CD, USER_LV
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public String[] get_user_info(String uid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String[] rtnstr = new String[5];
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT USER_PW, DEP_CD, ORG_CD, USER_LV, AUTH_SEQ FROM TB_BAS_USER WHERE USER_ID='" + uid + "'");

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			rs = stmt.executeQuery();
			
			if(rs.next()) {
				rtnstr[0] = rs.getString(1);
				rtnstr[1] = rs.getString(2);
				rtnstr[2] = rs.getString(3);
				rtnstr[3] = rs.getString(4);
				rtnstr[4] = rs.getString(5);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return rtnstr;
	}
	
	/**
	 * uauto Select Query
	 * @param uid : �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�떛�벝�삕
	 * @return "ID|ORG_CD|DEP_CD|ORG_NO|PTAB|VTAB|DTAB|USER_LV|TRANS_NO "
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public String[] get_user_uauth(String uid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String[] rtnstr = new String[11];
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT t2.USER_ID, t2.ORG_CD, t2.DEP_CD , t1.ORG_NO , t1.PTAB , t1.VTAB , t1.DTAB , t2.USER_LV,t2.TRANS_NO,t2.AUTH_SEQ,t2.USER_NM FROM TB_BAS_ORG t1 ");
			strbuf.append("INNER JOIN TB_BAS_USER t2 ");
			strbuf.append("ON (t1.ORG_CD=t2.ORG_CD) ");
			strbuf.append("where t2.USER_ID = '" + uid +"'");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString()); //�뜝�떥源띿삕

			rs = stmt.executeQuery();
			
			if(rs.next()) {
				rtnstr[0] = rs.getString(1);
				rtnstr[1] = rs.getString(2);
				rtnstr[2] = rs.getString(3);
				rtnstr[3] = rs.getString(4);
				rtnstr[4] = rs.getString(5);
				rtnstr[5] = rs.getString(6);
				rtnstr[6] = rs.getString(7);
				rtnstr[7] = rs.getString(8);
				rtnstr[8] = rs.getString(9);
				rtnstr[9] = rs.getString(10);
				rtnstr[10] = rs.getString(11);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return rtnstr;
	}
	
	/**
	 * TRANS_NO Select Query
	 * @param uid : �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�떛�벝�삕
	 * @return trans_no
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public String get_user_trans_no(String uid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String rtnstr = "";
		try {
			strbuf = new StringBuffer();
			strbuf.append("select trans_no from tb_bas_user where user_id = '" + uid + "'");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
				rs = stmt.executeQuery();
			
			if(rs.next()) {
				rtnstr = rs.getString(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return rtnstr;
	}
	
	/**
	 * dhx �뜝�떆琉꾩삕 Select Query
	 * @param orgcd,pages
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_tb_sys_domain(String orgcd,String pages) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("select * from tb_sys_domain where orgcd = '" + orgcd + "' and pages = '" + pages + "' order by orn asc");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				JSONObject jsonob2 = new JSONObject();
				JSONObject jsonob_header = new JSONObject();
				JSONObject jsonob_header2 = new JSONObject();
				JSONObject jsonob_header3 = new JSONObject();
				JSONArray jsonary_header = new JSONArray();
				JSONArray jsonary3 = new JSONArray();

            	JSONObject jsonob_colheader = new JSONObject();
				JSONArray jsonary_colheader = new JSONArray();

	            jsonob.put("width",rs.getInt("WIDTHS"));
	            jsonob.put("id",rs.getString("POS_FIELD"));
	            
	            
	            if(rs.getInt("ROWSPAN") != 0) {
		            jsonob_header.put("text",rs.getString("FIELDS_TXT"));
	            	jsonob_header.put("rowspan",rs.getInt("ROWSPAN"));	
	            	jsonob_header.put("align",rs.getString("ALIGNS"));
	            	jsonary_header.add(jsonob_header);
	            	
	            }
				
	            if(rs.getInt("COLSPAN") != 0) {
		            jsonob_header.put("text",rs.getString("FIELDS_TXT"));
	            	jsonob_header.put("colspan",rs.getInt("COLSPAN"));	
	            	jsonob_header.put("align",rs.getString("ALIGNS"));
	            	jsonary_header.add(jsonob_header);
	            	jsonob_colheader.put("text",rs.getString("COL_TXT"));
	            	jsonary_header.add(jsonob_colheader);
	            }

	            if(rs.getInt("COLSPAN") == 0 && rs.getInt("ROWSPAN") == 0 && Objects.equals(rs.getString("COL_CHK"), "Y")) {
					JSONObject jsonob_header_test = new JSONObject();
					jsonob_header_test.put("text2","");	
		            jsonob_header.put("text",rs.getString("FIELDS_TXT"));
	            	jsonob_header.put("align",rs.getString("ALIGNS"));
		            jsonary_header.add(jsonob_header_test);
		            jsonary_header.add(jsonob_header);
	            }
	            
	            if(rs.getInt("COLSPAN") == 0 && rs.getInt("ROWSPAN") == 0 && !Objects.equals(rs.getString("COL_CHK"), "Y")) {
					JSONObject jsonob_header_test = new JSONObject();
		            jsonob_header.put("text",rs.getString("FIELDS_TXT"));
	            	jsonob_header.put("align",rs.getString("ALIGNS"));
		            jsonary_header.add(jsonob_header);
	            }
	            
	            
	            if(rs.getString("SORTS").equals("int")) {
	            jsonob.put("format","#,###");
	            }
	           
	            jsonob.put("header",jsonary_header);
				jsonary.add(jsonob);

				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * dhx �뜝�떆琉꾩삕 Select Query �뜝�떗�눦�삕�뜝�룞�삕�뜝�룞�삕�쉶
	 * @param orgcd,pages
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕 (�뜝�떙�궪�삕 �뜝�떆琉꾩삕�솗�뜝�룞�삕 �뜝�룞�삕)
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_tb_sys_domain_sel(String orgcd,String pages) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("select * from tb_sys_domain where orgcd = '" + orgcd + "' and pages = '" + pages + "' order by orn asc");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				jsonob.put("id",rs.getString("POS_FIELD"));
				jsonary.add(jsonob);

				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		System.out.println(jsonary);
		return jsonary;
	}
	
	/**
	 * get_sub0201 glob_mng_icvan Select Query 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0201(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
				
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		
		try {
			strbuf = new StringBuffer();			
			strbuf.append("SELECT \r\n");
			strbuf.append("    SEQNO, \r\n");
			strbuf.append("	   DEP_NM TR_DEPNM, \r\n");
			strbuf.append("	   TERM_NM TR_TIDNM, \r\n");
			strbuf.append("	   TO_CHAR(TO_DATE(APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD,\r\n");
			strbuf.append("	   TO_CHAR(TO_DATE(APPTM, 'HH24MISS'), 'HH24:MI:SS') TR_APPTM,\r\n");
			strbuf.append("	   APPGB_TXT TR_AUTHSTAT,\r\n");
			strbuf.append("	   ADD_CID ADD_PID,\r\n");
			strbuf.append("	   ADD_CASHER ADD_CID,\r\n");
			strbuf.append("	   PUR_NM TR_ACQNM, \r\n");
			strbuf.append("	   CARDNO TR_CARDNO,	\r\n");
			strbuf.append("	   APPNO TR_APPNO, \r\n");
			strbuf.append("	   TRIM(TO_CHAR((AMOUNT),'999,999,999,999,999,999')) TR_AMT,	\r\n");
			strbuf.append("    HALBU TR_HALBU, \r\n");
			strbuf.append("    APP_MSG,\r\n");
			strbuf.append("    OAPP_AMT TR_CANDD,\r\n");
			strbuf.append("	   TO_CHAR(TO_DATE(OAPPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_OAPPDD,\r\n");
			strbuf.append("	   CARDTP_TXT TR_CARDTP, \r\n");
			strbuf.append("    OVSEA_CARD, \r\n");			
			strbuf.append("	   MID TR_MID, \r\n");
			strbuf.append("	   ADD_CD ADD_RHK, \r\n"); //
			strbuf.append("	   ADD_GB ADD_PGB,\r\n");
			strbuf.append("	   TID TR_TID, \r\n");
			strbuf.append("	   TO_CHAR(TO_DATE(EXP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') DP_EXP_DD, \r\n");
			strbuf.append("	   RTN_TXT DP_RST_TXT, \r\n");
			strbuf.append("	   TO_CHAR(TO_DATE(DEPO_DD, 'YYYYMMDD'), 'YYYY-MM-DD') DP_REQ_DD, \r\n");
			strbuf.append("	   TO_CHAR(TO_DATE(REQ_DD, 'YYYYMMDD'), 'YYYY-MM-DD') DP_RES_DD, \r\n");
			strbuf.append("	   TO_CHAR(TO_DATE(REG_DD, 'YYYYMMDD'), 'YYYY-MM-DD') DP_REG_DD, \r\n");
			strbuf.append("	   TRANIDX HIDDEN \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("	SELECT \r\n");
			strbuf.append("		SEQNO, DEP_NM, TERM_NM, TID, MID, PUR_NM, \r\n");
			strbuf.append("		APPDD, APPTM,  OAPPDD, APPNO, OAPP_AMT,APPGB, \r\n");
			strbuf.append("		CARDNO,	AMOUNT,	HALBU, \r\n");
			strbuf.append("		REQ_DD,	AUTHCD,	REG_DD,	RTN_CD, \r\n");
			strbuf.append("		EXP_DD,	EXT_FIELD,	TRANIDX, AUTHMSG, \r\n");
			strbuf.append("		ADD_GB, ADD_CID, ADD_CD, ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, DEPO_DD,  \r\n");
			strbuf.append("		CASE \r\n");
			strbuf.append("			WHEN OVSEA_CARD='Y' THEN '해외카드' \r\n");
			strbuf.append("			WHEN OVSEA_CARD='N' THEN '국내카드' \r\n");
			strbuf.append("		END OVSEA_CARD,\r\n");					
			strbuf.append("		CASE \r\n");
			strbuf.append("			WHEN APPGB='A' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0015')\r\n");
			strbuf.append("			WHEN APPGB='C' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0016')\r\n");
			strbuf.append("		END APPGB_TXT,\r\n");
			strbuf.append("		CASE \r\n");
			strbuf.append("			WHEN CHECK_CARD='Y' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0019') \r\n");
			strbuf.append("			ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0018') \r\n");
			strbuf.append("		END CARDTP_TXT,\r\n");
			strbuf.append("		CASE \r\n");
			strbuf.append("			WHEN SIGNCHK='1' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0021') \r\n");
			strbuf.append("			ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0022')  \r\n");
			strbuf.append("	    END SIGNCHK_TXT,\r\n");
			strbuf.append("		CASE \r\n");
			strbuf.append("			WHEN RTN_CD IS NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0024') \r\n");
			strbuf.append("			WHEN RTN_CD IN('60', '67') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0025')\r\n");
			strbuf.append("			WHEN RTN_CD IN('61', '64') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0026') \r\n");
			strbuf.append("		END RTN_TXT,\r\n");
			strbuf.append("		CASE \r\n");
			strbuf.append("	    	WHEN TLINEGB IS NOT NULL THEN (SELECT CODE_VAL FROM TB_BAS_CODE WHERE TRIM(CODE_NO)=TRIM(TLINEGB)) \r\n");
			strbuf.append("	    END TLINEGBTXT, \r\n");
			strbuf.append("		CASE \r\n");
			strbuf.append("	        WHEN APPGB='A' AND (OAPP_AMT IS NULL OR OAPP_AMT = 0) THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0011') \r\n"); //�뜝�룞�삕�뜝�룞�삕�궧�뜝占� 
			strbuf.append("	        WHEN APPGB='A' AND OAPP_AMT=APPDD   THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0012') \r\n"); //�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝占�
			strbuf.append("	        WHEN APPGB='A' AND OAPP_AMT<>APPDD THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0013')  \r\n");
			strbuf.append("	        WHEN APPGB='C' AND OAPPDD=APPDD  THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0012')  \r\n");
			strbuf.append("	        WHEN APPGB='C' AND OAPPDD<>APPDD THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0013')  \r\n"); //�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝占�			
			strbuf.append("	    END APP_MSG \r\n");
			strbuf.append("	FROM(\r\n");
			strbuf.append("		SELECT\r\n");
			strbuf.append("			SEQNO, BIZNO, TID, MID, VANGB, MDATE, SVCGB, T1.TRANIDX, T1.APPGB, ENTRYMD,\r\n");
			strbuf.append("			T1.APPDD, APPTM, T1.APPNO, T1.CARDNO, HALBU, CURRENCY, T1.AMOUNT, AMT_UNIT, AMT_TIP, AMT_TAX,\r\n");
			strbuf.append("			ISS_CD, ISS_NM, ACQ_CD, ACQ_NM, AUTHCD, AUTHMSG, CARD_CODE, CHECK_CARD, OVSEA_CARD, TLINEGB,\r\n");
			strbuf.append("			SIGNCHK, DDCGB, EXT_FIELD, OAPPNO, OAPPDD, OAPPTM, OAPP_AMT, ADD_GB, ADD_CID, ADD_CD,\r\n");
			strbuf.append("			ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, SECTION_NO, PUR_NM, DEP_NM, EXP_DD, REQ_DD, REG_DD, RSC_CD, RTN_CD, TERM_NM,\r\n");
			strbuf.append("			DEPOREQDD DEPO_DD\r\n");
			strbuf.append("		FROM\r\n");
			strbuf.append("			GLOB_MNG_ICVAN T1\r\n");
			strbuf.append("		LEFT OUTER JOIN(\r\n");
			strbuf.append("			SELECT EXP_DD, REQ_DD, REG_DD, APP_DD, TRANIDX, RSC_CD, RTN_CD FROM TB_MNG_DEPDATA\r\n");
			strbuf.append("		)T2 ON(T1.APPDD=T2.APP_DD AND T1.TRANIDX=T2.TRANIDX)\r\n");
			strbuf.append("		LEFT OUTER JOIN( \r\n");
			strbuf.append("			SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd + "'\r\n");
			strbuf.append("		)T3 ON(T1.TID=T3.TERM_ID)\r\n");
			strbuf.append("		LEFT OUTER JOIN( \r\n");
			strbuf.append("			SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "'\r\n");
			strbuf.append("		)T4 ON(T3.DEP_CD=T4.DEP_CD)\r\n");
			strbuf.append("		LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_CD FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_CD)\r\n");
			strbuf.append("		WHERE SVCGB IN ('CC', 'CE')  AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where ORG_CD='" + orgcd + "' " + depcd_where + ") " + set_where + "\r\n");
			strbuf.append("		order by APPDD desc, apptm desc\r\n");
			strbuf.append("	)\r\n");
			strbuf.append(")\r\n");
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			//if(Objects.equals(DEBUG,"Y")) {
			if(Objects.equals(DEBUG, "Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {					
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					//System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					//System.out.println("�뜝�룞�삕�뜝�룞�삕");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
				}
	            
				jsonary.add(jsonob);

				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * glob_mng_icvan_tot Select Query
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0201T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
			
		
		try {
			strbuf = new StringBuffer();			
			strbuf.append("SELECT  \r\n");
			strbuf.append("    DEP_NM,  \r\n");
			strbuf.append("    TERM_ID,  \r\n");
			strbuf.append("    TERM_NM,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT),  '999,999,999,999,999,999')) ACNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((CCNT),  '999,999,999,999,999,999')) CCNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT),  '999,999,999,999,999,999')) AAMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((CAMT),  '999,999,999,999,999,999')) CAMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((TOTCNT),'999,999,999,999,999,999')) TOTCNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((TOTAMT),'999,999,999,999,999,999')) TOTAMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((BC),    '999,999,999,999,999,999')) BC,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((NH),    '999,999,999,999,999,999')) NH,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((KB),    '999,999,999,999,999,999')) KB,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SS),    '999,999,999,999,999,999')) SS,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((HN),    '999,999,999,999,999,999')) HN,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((LO),    '999,999,999,999,999,999')) LO,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((HD),    '999,999,999,999,999,999')) HD,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SI),    '999,999,999,999,999,999')) SI,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((GD),    '999,999,999,999,999,999')) GD,  \r\n");
			strbuf.append("    '0' ZERO,'0' KAKAO,'0' HN, '0' JH, '0' AP, '0' WP \r\n");
			strbuf.append("FROM(    \r\n");
			strbuf.append("    SELECT  \r\n");
			strbuf.append("        TID,  \r\n");
			strbuf.append("        SUM(ACNT)   ACNT, \r\n");
			strbuf.append("        SUM(CCNT)   CCNT, \r\n");
			strbuf.append("        SUM(AAMT)   AAMT, \r\n");
			strbuf.append("        SUM(CAMT)   CAMT, \r\n");
			strbuf.append("        SUM(ACNT) + SUM(CCNT) TOTCNT, \r\n");
			strbuf.append("        SUM(AAMT) - SUM(CAMT) TOTAMT, \r\n");
			strbuf.append("        SUM(ABC ) - SUM(CBC ) BC, \r\n");
			strbuf.append("        SUM(ANH ) - SUM(CNH ) NH, \r\n");
			strbuf.append("        SUM(AKB ) - SUM(CKB ) KB, \r\n");
			strbuf.append("        SUM(ASS ) - SUM(CSS ) SS, \r\n");
			strbuf.append("        SUM(AHN ) - SUM(CHN ) HN, \r\n");
			strbuf.append("        SUM(ALO ) - SUM(CLO ) LO, \r\n");
			strbuf.append("        SUM(AHD ) - SUM(CHD ) HD, \r\n");
			strbuf.append("        SUM(ASI ) - SUM(CSI ) SI, \r\n");
			strbuf.append("        SUM(AGD ) - SUM(CGD ) GD \r\n");
			strbuf.append("    FROM(    \r\n");
			strbuf.append("        SELECT  \r\n");
			strbuf.append("            TID,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' THEN COUNT(1) ELSE 0 END ACNT,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' THEN COUNT(1) ELSE 0 END CCNT,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0006', '026', '1106', '01') THEN SUM(AMOUNT) ELSE 0 END ABC,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0030', '018', '2211', '11') THEN SUM(AMOUNT) ELSE 0 END ANH,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0001', '016', '1101', '02') THEN SUM(AMOUNT) ELSE 0 END AKB,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0004', '031', '1104', '06') THEN SUM(AMOUNT) ELSE 0 END ASS, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0005', '008', '1105', '03') THEN SUM(AMOUNT) ELSE 0 END AHN,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0003', '047', '1103', '33') THEN SUM(AMOUNT) ELSE 0 END ALO,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0002', '027', '1102', '08') THEN SUM(AMOUNT) ELSE 0 END AHD,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0007', '029', '1107', '07') THEN SUM(AMOUNT) ELSE 0 END ASI,  \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0008', '013', '1113') THEN SUM(AMOUNT) ELSE 0 END AGD,  \r\n");			
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0006', '026', '1106', '01') THEN SUM(AMOUNT) ELSE 0 END CBC,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0030', '018', '2211', '11') THEN SUM(AMOUNT) ELSE 0 END CNH,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0001', '016', '1101', '02') THEN SUM(AMOUNT) ELSE 0 END CKB,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0004', '031', '1104', '06') THEN SUM(AMOUNT) ELSE 0 END CSS,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0005', '008', '1105', '03') THEN SUM(AMOUNT) ELSE 0 END CHN,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0003', '047', '1103', '33') THEN SUM(AMOUNT) ELSE 0 END CLO,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0002', '027', '1102', '08') THEN SUM(AMOUNT) ELSE 0 END CHD,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0007', '029', '1107', '07') THEN SUM(AMOUNT) ELSE 0 END CSI,  \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0008', '013', '1113') THEN SUM(AMOUNT) ELSE 0 END CGD  \r\n");
			strbuf.append("        FROM (  \r\n");
			strbuf.append("            SELECT  \r\n");
			strbuf.append("                SEQNO, \r\n");			
			strbuf.append("                DEP_NM, TID, TERM_NM, APPGB, \r\n");
			strbuf.append("                MID, PUR_NM, ACQ_CD, \r\n");
			strbuf.append("                APPGB_TXT, APPDD, APPTM, OAPPDD, APPNO, \r\n");
			strbuf.append("                TR_AUTHSTAT, CARDNO, AMOUNT, HALBU, CARDTP_TXT, SIGNCHK_TXT,\r\n");
			strbuf.append("                REQ_DD, AUTHCD, REG_DD, RTN_CD, RTN_TXT, \r\n");
			strbuf.append("                EXP_DD, EXT_FIELD, TRANIDX, AUTHMSG\r\n");
			strbuf.append("            FROM(\r\n");
			strbuf.append("                SELECT\r\n");
			strbuf.append("                    RNUM, SEQNO, DEP_NM, TERM_NM, TID, MID, PUR_NM, APPGB,\r\n");
			strbuf.append("                CASE \r\n");
			strbuf.append("                    WHEN APPGB='A' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0011')\r\n");
			strbuf.append("                    WHEN APPGB='C' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0012')\r\n");
			strbuf.append("                END APPGB_TXT,\r\n");
			strbuf.append("                APPDD, APPTM, OAPPDD, APPNO, ACQ_CD,\r\n");
			strbuf.append("                CASE \r\n");
			strbuf.append("                    WHEN APPGB='A' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0015')\r\n");
			strbuf.append("                    WHEN APPGB='C' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0016')\r\n");
			strbuf.append("                END TR_AUTHSTAT,\r\n");
			strbuf.append("                CARDNO, AMOUNT, HALBU,\r\n");
			strbuf.append("                CASE \r\n");
			strbuf.append("                    WHEN CHECK_CARD='Y' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0019') \r\n");
			strbuf.append("                    ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0018') \r\n");
			strbuf.append("                END CARDTP_TXT,\r\n");
			strbuf.append("                CASE \r\n");
			strbuf.append("                    WHEN SIGNCHK='1' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0021') \r\n");
			strbuf.append("                    ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0022') \r\n");
			strbuf.append("                END SIGNCHK_TXT, \r\n");
			strbuf.append("                REQ_DD, AUTHCD, REG_DD, RTN_CD, \r\n");
			strbuf.append("                CASE  \r\n");
			strbuf.append("                    WHEN RTN_CD IS NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0024') \r\n");
			strbuf.append("                    WHEN RTN_CD IN('60', '67') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0025')\r\n");
			strbuf.append("                    WHEN RTN_CD IN('61', '64') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0026') \r\n");
			strbuf.append("                END RTN_TXT,  \r\n");
			strbuf.append("                EXP_DD, EXT_FIELD, TRANIDX, AUTHMSG  \r\n");
			strbuf.append("                FROM(  \r\n");
			strbuf.append("                    SELECT  \r\n");
			strbuf.append("                        ROWNUM AS RNUM,  \r\n");
			strbuf.append("                        SEQNO, BIZNO, TID, MID, VANGB, MDATE, SVCGB, T1.TRANIDX, T1.APPGB, ENTRYMD,  \r\n");
			strbuf.append("                        T1.APPDD, APPTM, T1.APPNO, T1.CARDNO, HALBU, CURRENCY, T1.AMOUNT, AMT_UNIT, AMT_TIP, AMT_TAX,\r\n");
			strbuf.append("                        ISS_CD, ISS_NM, ACQ_CD, ACQ_NM, AUTHCD, AUTHMSG, CARD_CODE, CHECK_CARD, OVSEA_CARD, TLINEGB,\r\n");
			strbuf.append("                        SIGNCHK, DDCGB, EXT_FIELD, OAPPNO, OAPPDD, OAPPTM, OAPP_AMT, ADD_GB, ADD_CID, ADD_CD,\r\n");
			strbuf.append("                        ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, SECTION_NO, PUR_NM, DEP_NM, EXP_DD, REQ_DD, REG_DD, RSC_CD, RTN_CD, TERM_NM\r\n");
			strbuf.append("                    FROM  \r\n");
			strbuf.append("                        GLOB_MNG_ICVAN T1\r\n");
			strbuf.append("                    LEFT OUTER JOIN(\r\n");
			strbuf.append("                        SELECT EXP_DD, REQ_DD, REG_DD, APP_DD, TRANIDX, RSC_CD, RTN_CD FROM TB_MNG_DEPDATA\r\n");
			strbuf.append("                    )T2 ON(T1.APPDD=T2.APP_DD AND T1.TRANIDX=T2.TRANIDX)\r\n");
			strbuf.append("                    LEFT OUTER JOIN( SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd + "')T3 ON(T1.TID=T3.TERM_ID)\r\n");
			strbuf.append("                    LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "')T4 ON(T3.DEP_CD=T4.DEP_CD)\r\n");
			strbuf.append("                    LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_KOCES FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES)\r\n");
			strbuf.append("                    WHERE SVCGB IN ('CC', 'CE')  AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where ORG_CD='" + orgcd + "' " + depcd_where + ")  " + set_where + "\r\n");
			strbuf.append("                    order by APPDD desc, apptm desc\r\n");
			strbuf.append("                )  \r\n");
			strbuf.append("            )  \r\n");
			strbuf.append("        ) GROUP BY TID, APPGB, ACQ_CD  \r\n");
			strbuf.append("    ) GROUP BY TID \r\n");
			strbuf.append(")T2\r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd + "')T3 ON(T2.TID=T3.TERM_ID)\r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "')T4 ON(T3.DEP_CD=T4.DEP_CD)");


			//System.lineSeparator()
		
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {
					
					
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}

	            
				jsonary.add(jsonob);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * get_sub0202 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0202(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);

		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕

			strbuf.append("SELECT \r\n");
			strbuf.append("	   DEP_NM, \r\n");
			strbuf.append("    PUR_NM, \r\n");
			strbuf.append("    T1.MID, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT+CCNT),'999,999,999,999,999,999')) TOTCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT-CAMT),'999,999,999,999,999,999')) TOTAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT),'999,999,999,999,999,999')), \r\n");
			strbuf.append("    TRIM(TO_CHAR((CCNT),'999,999,999,999,999,999')), \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT),'999,999,999,999,999,999')), \r\n");
			strbuf.append("    TRIM(TO_CHAR((CAMT),'999,999,999,999,999,999')), \r\n");
			strbuf.append("    ACQ_CD HIDDEN \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("			SELECT \r\n");
			strbuf.append("				MID, ACQ_CD, APPDD, SUM(ACNT) ACNT, SUM(CCNT) CCNT, SUM(AAMT) AAMT, SUM(CAMT) CAMT \r\n");
			strbuf.append("			FROM( \r\n");
			strbuf.append("				SELECT \r\n");
			strbuf.append("					MID,  \r\n");
			strbuf.append("					ACQ_CD, \r\n");
			strbuf.append("					APPDD, \r\n");
			strbuf.append("					CASE WHEN APPGB='A' THEN COUNT(1) ELSE 0 END ACNT, \r\n");
			strbuf.append("					CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT, \r\n");
			strbuf.append("					CASE WHEN APPGB='C' THEN COUNT(1) ELSE 0 END CCNT,\r\n");
			strbuf.append("					CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT  \r\n");
			strbuf.append("				FROM  \r\n");
			strbuf.append("					GLOB_MNG_ICVAN \r\n");
			strbuf.append("				WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where ORG_CD='" + orgcd + "')" + set_where + "\r\n");
			strbuf.append("				GROUP BY MID, ACQ_CD, APPDD, APPGB \r\n");
			strbuf.append("				)T1 \r\n");
			strbuf.append("			GROUP BY MID, ACQ_CD, APPDD \r\n");
			strbuf.append("			ORDER BY MID \r\n");
			strbuf.append("			)T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("		SELECT MER_NO, PUR_CD, DEP_CD FROM TB_BAS_MERINFO WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append("		)TM ON(T1.MID=TM.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "')T4 ON(TM.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN(  \r\n");
			strbuf.append("		SELECT PUR_NM, PUR_KOCES, PUR_SORT,PUR_CD, PUR_OCD FROM TB_BAS_PURINFO \r\n");
			strbuf.append("		)T5 ON(T1.ACQ_CD=T5.PUR_KOCES OR T1.ACQ_CD=T5.PUR_OCD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("		SELECT ORG_CD, USER_PUR_CD, USER_PURSORT FROM TB_BAS_USERPURINFO WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append("		)S3 ON(T5.PUR_CD=S3.USER_PUR_CD) \r\n");
			strbuf.append("ORDER BY APPDD ASC, DEP_NM ASC, PUR_NM ASC \r\n");			
			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0203 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0203(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
	
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT  \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD,  \r\n");
			strbuf.append("    ADD_CASHER ADD_CID,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(KB_CNT)                      ),'999,999,999,999,999,999')) KB_CNT,  \r\n");			
			strbuf.append("    TRIM(TO_CHAR((SUM(KBA)        -  SUM(KBC)      ),'999,999,999,999,999,999')) KB_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(NH_CNT)                      ),'999,999,999,999,999,999')) NH_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(NHA)        -  SUM(NHC)      ),'999,999,999,999,999,999')) NH_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(LO_CNT)                      ),'999,999,999,999,999,999')) LO_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(LOA)        -  SUM(LOC)      ),'999,999,999,999,999,999')) LO_AMT,  \r\n");			
			strbuf.append("    TRIM(TO_CHAR((SUM(BC_CNT)                      ),'999,999,999,999,999,999')) BC_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(BCA)        -  SUM(BCC)      ),'999,999,999,999,999,999')) BC_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(SS_CNT)                      ),'999,999,999,999,999,999')) SS_CNT,  \r\n");   
			strbuf.append("    TRIM(TO_CHAR((SUM(SSA)        -  SUM(SSC)      ),'999,999,999,999,999,999')) SS_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(SI_CNT)                      ),'999,999,999,999,999,999')) SI_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(SIA)        -  SUM(SIC)      ),'999,999,999,999,999,999')) SI_AMT,  \r\n");			
			strbuf.append("    TRIM(TO_CHAR((SUM(HN_CNT)                      ),'999,999,999,999,999,999')) HN_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(HNA)        -  SUM(HNC)      ),'999,999,999,999,999,999')) HN_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(HD_CNT)                      ),'999,999,999,999,999,999')) HD_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(HDA)        -  SUM(HDC)      ),'999,999,999,999,999,999')) HD_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(GD_CNT)                      ),'999,999,999,999,999,999')) JH_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(GDA)        -  SUM(GDC)      ),'999,999,999,999,999,999')) JH_AMT,  \r\n");			
			strbuf.append("    TRIM(TO_CHAR((SUM(AP_CNT)                      ),'999,999,999,999,999,999')) AP_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(APA)        -  SUM(APC)      ),'999,999,999,999,999,999')) AP_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(WP_CNT)                      ),'999,999,999,999,999,999')) WP_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(WPA)        -  SUM(WPC)      ),'999,999,999,999,999,999')) WP_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(ZERO_CNT)                    ),'999,999,999,999,999,999')) ZERO_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(ZEROA)      -  SUM(ZEROC)    ),'999,999,999,999,999,999')) ZERO_AMT,  \r\n");		
			strbuf.append("    TRIM(TO_CHAR((SUM(KAKAO_CNT)                   ),'999,999,999,999,999,999')) KAKAO_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(KAKAOA)     -  SUM(KAKAOC)   ),'999,999,999,999,999,999')) KAKAO_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CASH_CNT)                    ),'999,999,999,999,999,999')) CASH_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CASHA)      -  SUM(CASHC)    ),'999,999,999,999,999,999')) CASH_AMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CASH_IC_CNT)                 ),'999,999,999,999,999,999')) CASH_IC_CNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CASH_IC_A)  -  SUM(CASH_IC_C)),'999,999,999,999,999,999')) CASH_IC_AMT  \r\n");
			strbuf.append("FROM(  \r\n");
			strbuf.append("    SELECT   \r\n");
			strbuf.append("        ADD_CASHER,  \r\n");
			strbuf.append("        APPDD,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0006', '026', '1106','01') THEN SUM(AMOUNT) ELSE 0 END BCA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0030', '018', '2211','11') THEN SUM(AMOUNT) ELSE 0 END NHA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0001', '016', '1101','02') THEN SUM(AMOUNT) ELSE 0 END KBA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0004', '031', '1104','06') THEN SUM(AMOUNT) ELSE 0 END SSA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0005', '008', '1105','03') THEN SUM(AMOUNT) ELSE 0 END HNA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0003', '047', '1103','33') THEN SUM(AMOUNT) ELSE 0 END LOA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0002', '027', '1102','08') THEN SUM(AMOUNT) ELSE 0 END HDA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0007', '029', '1107','07') THEN SUM(AMOUNT) ELSE 0 END SIA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('1113', '021')                THEN SUM(AMOUNT) ELSE 0 END GDA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0006', '026', '1106','01') THEN SUM(AMOUNT) ELSE 0 END BCC,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0030', '018', '2211','11') THEN SUM(AMOUNT) ELSE 0 END NHC,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0001', '016', '1101','02') THEN SUM(AMOUNT) ELSE 0 END KBC,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0004', '031', '1104','06') THEN SUM(AMOUNT) ELSE 0 END SSC,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0005', '008', '1105','03') THEN SUM(AMOUNT) ELSE 0 END HNC,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0003', '047', '1103','33') THEN SUM(AMOUNT) ELSE 0 END LOC,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0002', '027', '1102','08') THEN SUM(AMOUNT) ELSE 0 END HDC,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0007', '029', '1107','07') THEN SUM(AMOUNT) ELSE 0 END SIC,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('1113', '021')                THEN SUM(AMOUNT) ELSE 0 END GDC,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('VC0006', '026', '1106','01') THEN COUNT(1)    ELSE 0 END BC_CNT,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('VC0030', '018', '2211','11') THEN COUNT(1)    ELSE 0 END NH_CNT,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('VC0001', '016', '1101','02') THEN COUNT(1)    ELSE 0 END KB_CNT,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('VC0004', '031', '1104','06') THEN COUNT(1)    ELSE 0 END SS_CNT,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('VC0005', '008', '1105','03') THEN COUNT(1)    ELSE 0 END HN_CNT,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('VC0003', '047', '1103','33') THEN COUNT(1)    ELSE 0 END LO_CNT,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('VC0002', '027', '1102','08') THEN COUNT(1)    ELSE 0 END HD_CNT,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('VC0007', '029', '1107','07') THEN COUNT(1)    ELSE 0 END SI_CNT,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('1113', '021')                THEN COUNT(1)    ELSE 0 END GD_CNT,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('9999', '999')                THEN SUM(AMOUNT) ELSE 0 END APA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('9999', '999')                THEN SUM(AMOUNT) ELSE 0 END APC,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('9999', '999')                THEN COUNT(1)    ELSE 0 END AP_CNT,  \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('9998', '998')                THEN SUM(AMOUNT) ELSE 0 END WPA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('9998', '998')                THEN SUM(AMOUNT) ELSE 0 END WPC,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('9998', '998')                THEN COUNT(1)    ELSE 0 END WP_CNT, \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('9997', '997')                THEN SUM(AMOUNT) ELSE 0 END ZEROA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('9997', '997')                THEN SUM(AMOUNT) ELSE 0 END ZEROC,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('9997', '997')                THEN COUNT(1)    ELSE 0 END ZERO_CNT, \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('9996', '996')                THEN SUM(AMOUNT) ELSE 0 END KAKAOA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('9996', '996')                THEN SUM(AMOUNT) ELSE 0 END KAKAOC,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('9996', '996')                THEN COUNT(1)    ELSE 0 END KAKAO_CNT, \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('9995', '995')                THEN SUM(AMOUNT) ELSE 0 END CASHA,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('9995', '995')                THEN SUM(AMOUNT) ELSE 0 END CASHC,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('9995', '995')                THEN COUNT(1)    ELSE 0 END CASH_CNT, \r\n");
			strbuf.append("        CASE WHEN APPGB='A' AND ACQ_CD IN ('9994', '994')                THEN SUM(AMOUNT) ELSE 0 END CASH_IC_A,  \r\n");
			strbuf.append("        CASE WHEN APPGB='C' AND ACQ_CD IN ('9994', '994')                THEN SUM(AMOUNT) ELSE 0 END CASH_IC_C,  \r\n");
			strbuf.append("        CASE WHEN               ACQ_CD IN ('9994', '994')                THEN COUNT(1)    ELSE 0 END CASH_IC_CNT \r\n");
			strbuf.append("    FROM   \r\n");
			strbuf.append("        GLOB_MNG_ICVAN  \r\n");
			strbuf.append("    WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where ORG_CD='" + orgcd + "')"+ set_where +"\r\n");
			strbuf.append("    GROUP BY APPGB, APPDD, ADD_CASHER, ACQ_CD   \r\n");
			strbuf.append(")  \r\n");
			strbuf.append("GROUP BY ADD_CASHER, APPDD  \r\n");
			strbuf.append("ORDER BY ADD_CASHER ASC, APPDD ASC  \r\n");



			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0203T
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0203T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);

		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");			
			strbuf.append("    TRIM(TO_CHAR((ACNT)     ,'999,999,999,999,999,999')) ACNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((CCNT)     ,'999,999,999,999,999,999')) CCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT)     ,'999,999,999,999,999,999')) AAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((CAMT)     ,'999,999,999,999,999,999')) CAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT+CCNT),'999,999,999,999,999,999')) TOTCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT-CAMT),'999,999,999,999,999,999')) TOTAMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        SUM(ACNT) ACNT, SUM(CCNT) CCNT, SUM(AAMT) AAMT, SUM(CAMT) CAMT \r\n");
			strbuf.append("    FROM( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            CASE WHEN APPGB='A' THEN COUNT(1) ELSE 0 END ACNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' THEN COUNT(1) ELSE 0 END CCNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            GLOB_MNG_ICVAN \r\n");
			strbuf.append("        WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where ORG_CD='" + orgcd + "') "+ set_where + " \r\n");
			strbuf.append("        GROUP BY APPGB \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append(")T1 \r\n");
			
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}
			
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0204 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0204(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);

		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    DEP_NM, \r\n");
			strbuf.append("    TID, \r\n");
			strbuf.append("    TERM_NM, \r\n");
			strbuf.append("    PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    MID TR_MID, \r\n");
			strbuf.append("    ACQ_CD, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT)     ,'999,999,999,999,999,999')) ACNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((CCNT)     ,'999,999,999,999,999,999')) CCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT)     ,'999,999,999,999,999,999')) AAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((CAMT)     ,'999,999,999,999,999,999')) CAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT+CCNT),'999,999,999,999,999,999')) TOTCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT-CAMT),'999,999,999,999,999,999')) TOTAMT, \r\n");
			strbuf.append("    APPDD HIDDEN \r\n");
			strbuf.append("FROM ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        TID, MID, ACQ_CD, SUM(ACNT) ACNT, SUM(CCNT) CCNT, SUM(AAMT) AAMT, SUM(CAMT) CAMT, APPDD \r\n");
			strbuf.append("    FROM( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            TID, \r\n");
			strbuf.append("            MID, \r\n");
			strbuf.append("            ACQ_CD, \r\n");
			strbuf.append("            APPDD, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' THEN COUNT(1)    ELSE 0 END ACNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' THEN COUNT(1)    ELSE 0 END CCNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            GLOB_MNG_ICVAN \r\n");
			strbuf.append("        WHERE SVCGB IN ('CC', 'CE') AND AUTHCD IN ('0000', '6666') AND TID IN (select tid from tb_bas_tidmap where ORG_CD='" + orgcd + "') "+ set_where +" \r\n");
			strbuf.append("        GROUP BY TID, MID, ACQ_CD, APPGB, APPDD \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append("GROUP BY TID, MID, ACQ_CD, APPDD \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT PUR_NM, PUR_KOCES, PUR_OCD, PUR_SORT FROM TB_BAS_PURINFO )T2 ON(T1.ACQ_CD=T2.PUR_OCD OR T1.ACQ_CD=T2.PUR_KOCES ) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append(")T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append(")T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("ORDER BY TID, PUR_NM ASC \r\n");

			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0205 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0205(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
	
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    T3.DEP_CD, \r\n");
			strbuf.append("    DEP_NM, \r\n");
			strbuf.append("    TID TR_TID, \r\n");
			strbuf.append("    TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(ACNT))       ,'999,999,999,999,999,999'))  ACNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CCNT))       ,'999,999,999,999,999,999'))  CCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(AAMT))       ,'999,999,999,999,999,999'))  AAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CAMT))       ,'999,999,999,999,999,999'))  CAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(ACNT + CCNT)),'999,999,999,999,999,999'))  TOTCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(AAMT - CAMT)),'999,999,999,999,999,999'))  TOTAMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        TID, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='A' THEN COUNT(1) ELSE 0  \r\n");
			strbuf.append("        END ACNT, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 \r\n");
			strbuf.append("        END AAMT, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='C' THEN COUNT(1) ELSE 0 \r\n");
			strbuf.append("        END CCNT, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 \r\n");
			strbuf.append("        END CAMT \r\n");
			strbuf.append("    FROM  \r\n");
			strbuf.append("        GLOB_MNG_ICVAN \r\n");
			strbuf.append("        WHERE TID IN (SELECT TID FROM TB_BAS_TIDMAP WHERE ORG_CD = '" +orgcd + "')" + set_where +" \r\n");
			strbuf.append("    GROUP BY TID, APPGB \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT TERM_ID, TERM_NM, DEP_CD FROM TB_BAS_TIDMST WHERE ORG_CD = '" + orgcd + "' \r\n");
			strbuf.append(")T3 ON(T3.TERM_ID=T1.TID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_CD, DEP_NM FROM TB_BAS_DEPART WHERE ORG_CD = '" + orgcd + "' \r\n");
			strbuf.append(")T2 ON(T3.DEP_CD=T2.DEP_CD) \r\n");
			strbuf.append("GROUP BY T3.DEP_CD, DEP_NM, TID, TERM_NM \r\n");
			strbuf.append("ORDER BY T3.DEP_CD, TERM_NM \r\n");


			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0206 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0206(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);

		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPTM, 'HH24MISS'), 'HH24:MI:SS') TR_APPTM, \r\n");
			strbuf.append("    APPGB_TXT TR_AUTHSTAT, \r\n");
			strbuf.append("    ADD_CID ADD_PID, \r\n");
			strbuf.append("    ADD_CASHER ADD_CID, \r\n");
			strbuf.append("    APPNO TR_APPNO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AMOUNT),'999,999,999,999,999,999')) TR_AMT, \r\n");
			strbuf.append("    TRANTYPE CASH_TP, \r\n");
			strbuf.append("    CARDNO CASH_ID, \r\n");
			strbuf.append("    TSTAT_TXT APP_MSG, \r\n");
			strbuf.append("    TSTAT TR_CANDD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(OAPPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_OAPPDD, \r\n");
			strbuf.append("    ADD_CD ADD_RHK, \r\n");
			strbuf.append("    ADD_GB ADD_PGB, \r\n");
			strbuf.append("    TID TR_TID \r\n");			
			strbuf.append("FROM( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        DEP_NM, TERM_NM, APPDD, APPTM,  \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='A' THEN '신용승인' \r\n");
			strbuf.append("            WHEN APPGB='C' THEN '신용취소' \r\n");
			strbuf.append("        END APPGB_TXT, \r\n");			
			strbuf.append("        ADD_CID, ADD_CASHER, APPNO, AMOUNT, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN DDCGB='0' THEN '소득공제' \r\n");
			strbuf.append("            WHEN DDCGB='1' THEN '지출증빙' \r\n");
			strbuf.append("            ELSE '자진발급' \r\n");
			strbuf.append("        END TRANTYPE, \r\n");			
			strbuf.append("        CARDNO,  \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='A' AND OAPP_AMT IS NULL                         THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0011') \r\n");
			strbuf.append("            WHEN APPGB='A' AND OAPP_AMT=APPDD                           THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0012') \r\n");
			strbuf.append("            WHEN APPGB='C' AND APPDD=OAPPDD                             THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0012') \r\n");
			strbuf.append("            WHEN APPGB='C' AND APPDD<>OAPPDD                            THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0013') \r\n");
			strbuf.append("            WHEN APPGB='A' AND APPDD<>OAPP_AMT AND OAPP_AMT IS NOT NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0013') \r\n");
			strbuf.append("        END TSTAT_TXT, \r\n");
			strbuf.append("        TSTAT, OAPPDD, ADD_CD, TID, \r\n");			
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN ADD_GB IN ('1', 'O') THEN 'O' \r\n");
			strbuf.append("            WHEN ADD_GB IN ('2', 'E') THEN 'E' \r\n");
			strbuf.append("            WHEN ADD_GB IN ('3', 'I') THEN 'I' \r\n");
			strbuf.append("            WHEN ADD_GB IN ('4', 'G') THEN 'G' \r\n");
			strbuf.append("            WHEN ADD_GB='5'           THEN 'ADDGB5' \r\n");
			strbuf.append("            WHEN ADD_GB='6'           THEN 'ADDGB6' \r\n");
			strbuf.append("            ELSE '' \r\n");
			strbuf.append("        END ADD_GB \r\n");				
			strbuf.append("    FROM ( \r\n");
			strbuf.append("        SELECT \r\n");			
			strbuf.append("            DEP_NM, TERM_NM, T1.APPDD, APPTM, T1.APPGB, \r\n");
			strbuf.append("            ADD_CID, ADD_CASHER, T1.APPNO, T1.AMOUNT, DDCGB, \r\n");
			strbuf.append("            T1.CARDNO, OAPP_AMT, OAPPDD,  \r\n");
			strbuf.append("            CASE \r\n");
			strbuf.append("                WHEN APPGB='C' THEN '' \r\n");
			strbuf.append("                WHEN APPGB='A' THEN (SELECT C1.APPDD FROM GLOB_MNG_ICVAN C1 WHERE C1.APPGB='C' AND T1.APPDD=C1.OAPPDD AND T1.APPNO=C1.APPNO AND T1.AMOUNT=C1.AMOUNT AND T1.CARDNO=C1.CARDNO) \r\n");
			strbuf.append("            END TSTAT, \r\n");
			strbuf.append("            ADD_CD, TID, ADD_GB  \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("        LEFT OUTER JOIN( \r\n");
			strbuf.append("            SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append("        )T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("        LEFT OUTER JOIN( \r\n");
			strbuf.append("            SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append("        )T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("        LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_KOCES, PUR_CD FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES OR T1.ACQ_CD=T5.PUR_CD) \r\n");
			strbuf.append("        WHERE SVCGB IN ('CB') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where ORG_CD='" + orgcd + "') " + set_where + "\r\n");
			strbuf.append("        order by APPDD desc, apptm desc \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append(") \r\n");

			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0206T 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0206T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);

		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    DEP_NM, \r\n");
			strbuf.append("    TERM_ID, \r\n");
			strbuf.append("    TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT)  ,'999,999,999,999,999,999')) ACNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((CCNT)  ,'999,999,999,999,999,999')) CCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT)  ,'999,999,999,999,999,999')) AAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((CAMT)  ,'999,999,999,999,999,999')) CAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((TOTCNT),'999,999,999,999,999,999')) TOTCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((TOTAMT),'999,999,999,999,999,999')) TOTAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((BC)    ,'999,999,999,999,999,999')) BC, \r\n");
			strbuf.append("    TRIM(TO_CHAR((NH)    ,'999,999,999,999,999,999')) NH, \r\n");
			strbuf.append("    TRIM(TO_CHAR((KB)    ,'999,999,999,999,999,999')) KB, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SS)    ,'999,999,999,999,999,999')) SS, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HN)    ,'999,999,999,999,999,999')) HN, \r\n");
			strbuf.append("    TRIM(TO_CHAR((LO)    ,'999,999,999,999,999,999')) LO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HD)    ,'999,999,999,999,999,999')) HD, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SI)    ,'999,999,999,999,999,999')) SI \r\n");
			strbuf.append("FROM ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        TID, \r\n");
			strbuf.append("        SUM(ACNT) ACNT, \r\n");
			strbuf.append("        SUM(CCNT) CCNT, \r\n");
			strbuf.append("        SUM(AAMT) AAMT, \r\n");
			strbuf.append("        SUM(CAMT) CAMT, \r\n");
			strbuf.append("        SUM(ACNT)+SUM(CCNT) TOTCNT, \r\n");
			strbuf.append("        SUM(AAMT)-SUM(CAMT) TOTAMT, \r\n");
			strbuf.append("        SUM(ABC )-SUM(CBC ) BC, \r\n");
			strbuf.append("        SUM(ANH )-SUM(CNH ) NH, \r\n");
			strbuf.append("        SUM(AKB )-SUM(CKB ) KB, \r\n");
			strbuf.append("        SUM(ASS )-SUM(CSS ) SS, \r\n");
			strbuf.append("        SUM(AHN )-SUM(CHN ) HN, \r\n");
			strbuf.append("        SUM(ALO )-SUM(CLO ) LO, \r\n");
			strbuf.append("        SUM(AHD )-SUM(CHD ) HD, \r\n");
			strbuf.append("        SUM(ASI )-SUM(CSI ) SI \r\n");
			strbuf.append("    FROM( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            TID, \r\n");
			strbuf.append("            CASE WHEN APPGB='A'                                              THEN COUNT(1)    ELSE 0 END ACNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C'                                              THEN COUNT(1)    ELSE 0 END CCNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='A'                                              THEN SUM(AMOUNT) ELSE 0 END AAMT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C'                                              THEN SUM(AMOUNT) ELSE 0 END CAMT, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0006', '026', '1106','01') THEN SUM(AMOUNT) ELSE 0 END ABC, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0030', '018', '2211','11') THEN SUM(AMOUNT) ELSE 0 END ANH, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0001', '016', '1101','02') THEN SUM(AMOUNT) ELSE 0 END AKB, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0004', '031', '1104','06') THEN SUM(AMOUNT) ELSE 0 END ASS, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0005', '008', '1105','03') THEN SUM(AMOUNT) ELSE 0 END AHN, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0003', '047', '1103','33') THEN SUM(AMOUNT) ELSE 0 END ALO, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0002', '027', '1102','08') THEN SUM(AMOUNT) ELSE 0 END AHD, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0007', '029', '1107','07') THEN SUM(AMOUNT) ELSE 0 END ASI, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0006', '026', '1106','01') THEN SUM(AMOUNT) ELSE 0 END CBC, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0030', '018', '2211','11') THEN SUM(AMOUNT) ELSE 0 END CNH, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0001', '016', '1101','02') THEN SUM(AMOUNT) ELSE 0 END CKB, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0004', '031', '1104','06') THEN SUM(AMOUNT) ELSE 0 END CSS, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0005', '008', '1105','03') THEN SUM(AMOUNT) ELSE 0 END CHN, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0003', '047', '1103','33') THEN SUM(AMOUNT) ELSE 0 END CLO, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0002', '027', '1102','08') THEN SUM(AMOUNT) ELSE 0 END CHD, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0007', '029', '1107','07') THEN SUM(AMOUNT) ELSE 0 END CSI \r\n");
			strbuf.append("        FROM ( \r\n");
			strbuf.append("            SELECT \r\n");			
			strbuf.append("                DEP_NM, TID, TERM_NM, APPGB, ACQ_CD, AMOUNT \r\n");	                           
			strbuf.append("            FROM \r\n");
			strbuf.append("                GLOB_MNG_ICVAN T1 \r\n");                                       
			strbuf.append("            LEFT OUTER JOIN( SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd + "')T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("            LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "')T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("            LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_KOCES FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES) \r\n");
			strbuf.append("            WHERE SVCGB IN ('CB') AND AUTHCD='0000' \r\n");
			strbuf.append("                AND TID IN (select tid from tb_bas_tidmap where ORG_CD = '" + orgcd + "' " + depcd_where + ")" + set_where + " \r\n");
			strbuf.append("            order by APPDD desc, apptm desc \r\n");
			strbuf.append("        ) \r\n");
			strbuf.append("        GROUP BY TID, APPGB, ACQ_CD \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append("GROUP BY TID \r\n");
			strbuf.append(")T2 \r\n");			
			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0207 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0207(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);

		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT  \r\n");
			strbuf.append("    DEP_NM TR_DEPNM,  \r\n");
			strbuf.append("    TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T1.APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPTM, 'HH24MISS'), 'HH24:MI:SS') TR_APPTM, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN T1.APPGB='A' THEN '신용승인' \r\n");
			strbuf.append("        WHEN T1.APPGB='C' THEN '신용취소' \r\n");
			strbuf.append("    END TR_AUTHSTAT, \r\n");
			strbuf.append("    ADD_CID ADD_PID,  \r\n");
			strbuf.append("    ADD_CASHER ADD_CID,  \r\n");
			strbuf.append("    T1.APPNO TR_APPNO,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((T1.AMOUNT),'999,999,999,999,999,999')) TR_AMT,  \r\n");
			strbuf.append("    ADD_RECP ADD_RHK,  \r\n"); 
			strbuf.append("    ADD_CD ADD_PGB,  \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN T1.APPGB='C' THEN '' \r\n");
			strbuf.append("        WHEN T1.APPGB='A' THEN (SELECT C1.APPDD FROM GLOB_MNG_ICVAN C1 WHERE C1.APPGB='C' AND T1.APPDD=C1.OAPPDD AND T1.APPNO=C1.APPNO AND T1.AMOUNT=C1.AMOUNT AND T1.CARDNO=C1.CARDNO) \r\n");
			strbuf.append("    END TR_CANDD, \r\n");
			strbuf.append("    OAPPDD TR_OAPPDD,  \r\n");
			strbuf.append("    OAPPNO TR_OAPPNO,  \r\n");
			strbuf.append("    CASE  \r\n");
			strbuf.append("        WHEN T1.APPGB='A' AND OAPP_AMT IS NULL                         THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0011')  \r\n");
			strbuf.append("        WHEN T1.APPGB='A' AND OAPP_AMT=APPDD                           THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0012')  \r\n");
			strbuf.append("        WHEN T1.APPGB='C' AND APPDD=OAPPDD                             THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0012')  \r\n");
			strbuf.append("        WHEN T1.APPGB='C' AND APPDD<>OAPPDD                            THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0013')  \r\n");
			strbuf.append("        WHEN T1.APPGB='A' AND APPDD<>OAPP_AMT AND OAPP_AMT IS NOT NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0013')  \r\n");
			strbuf.append("    END APP_MSG,  \r\n");
			strbuf.append("    TRIM(TO_CHAR(  \r\n");
			strbuf.append("        CASE  \r\n");
			strbuf.append("            WHEN TRUNC(AMOUNT*0.01) < 10 THEN 10 ELSE TRUNC(AMOUNT*0.01)   \r\n");
			strbuf.append("        END,   \r\n");
			strbuf.append("    '999,999,999,999,999,999')) CIC_FEE,  \r\n");
			strbuf.append("    TRIM(TO_CHAR(  \r\n");
			strbuf.append("        CASE  \r\n");
			strbuf.append("            WHEN TRUNC(AMOUNT*0.01) < 10 THEN (AMOUNT-10) ELSE (AMOUNT - TRUNC(AMOUNT*0.01)) \r\n");
			strbuf.append("        END,   \r\n");
			strbuf.append("    '999,999,999,999,999,999')) DP_EXP_AMT,  \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T6.EXPDD, 'YYYYMMDD'), 'YYYY-MM-DD') CIC_EXP_DD,  \r\n");
			strbuf.append("    'CASHBACK_STAT' CASHBACK_STAT, \r\n");
			strbuf.append("    'CIC_AMT_CASHBACK' CIC_AMT_CASHBACK, \r\n");
			strbuf.append("    'CIC_DXP_YN' CIC_DXP_YN, \r\n");
			strbuf.append("    TID TR_TID,  \r\n");
			strbuf.append("    MID TR_MID,  \r\n");
			strbuf.append("    ISS_NM CIC_ISSCD,  \r\n");
			strbuf.append("    ACQ_NM CIC_ACQCD, \r\n");
			strbuf.append("    T1.SEQNO CIC_ICSEQNO,  \r\n");
			strbuf.append("    'CLIENT_TP' CLIENT_TP  \r\n");		
			strbuf.append("FROM  \r\n");
			strbuf.append("    GLOB_MNG_ICVAN T1  \r\n");
			strbuf.append("LEFT OUTER JOIN(  \r\n");
			strbuf.append("    SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd +"'  \r\n");
			strbuf.append(")T3 ON(T1.TID=T3.TERM_ID)  \r\n");
			strbuf.append("LEFT OUTER JOIN(  \r\n");
			strbuf.append("    SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "'  \r\n");
			strbuf.append(")T4 ON(T3.DEP_CD=T4.DEP_CD)  \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT PUR_NM, PUR_NICE FROM TB_BAS_PURINFO)T5 ON (T1.ISS_CD=T5.PUR_NICE)  \r\n");
			strbuf.append("LEFT OUTER JOIN(  \r\n");
			strbuf.append("    SELECT SEQNO, TRANIDX, APPGB CICAPPGB, APPNO CICAPPNO, EXPDD FROM GLOB_MNG_CICEXP  \r\n");
			strbuf.append(")T6 ON (T1.SEQNO=T6.SEQNO AND T1.TRANIDX=T6.TRANIDX AND T1.APPGB=T6.CICAPPGB AND T1.APPNO=T6.CICAPPNO)  \r\n");
			strbuf.append("WHERE SVCGB IN ('IC') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where org_cd='" + orgcd + "') " + set_where + " \r\n");
			strbuf.append("ORDER BY APPDD ASC, APPTM ASC  \r\n");


			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0207T 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0207T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
	
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(ACNT))      ,'999,999,999,999,999,999'))  ACNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CCNT))      ,'999,999,999,999,999,999'))  CCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(AAMT))      ,'999,999,999,999,999,999'))  AAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(AFEE))      ,'999,999,999,999,999,999'))  AFEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CAMT))      ,'999,999,999,999,999,999'))  CAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CFEE))      ,'999,999,999,999,999,999'))  CFEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(TOTCNT))    ,'999,999,999,999,999,999'))  TOTCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(TOTAMT))    ,'999,999,999,999,999,999'))  TOTAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(TOTFEE))    ,'999,999,999,999,999,999'))  TOTFEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(DP_EXP_AMT)),'999,999,999,999,999,999'))  DP_EXP_AMT \r\n");
			strbuf.append("FROM( \r\n");			
			strbuf.append("    SELECT \r\n");
			strbuf.append("        ACNT, \r\n");
			strbuf.append("        CCNT, \r\n");
			strbuf.append("        AAMT, \r\n");
			strbuf.append("        AFEE, \r\n");
			strbuf.append("        CAMT, \r\n");
			strbuf.append("        CFEE, \r\n");
			strbuf.append("        TOTCNT, \r\n");
			strbuf.append("        TOTAMT, \r\n");
			strbuf.append("        TOTFEE, \r\n");
			strbuf.append("        TOTAMT-TOTFEE DP_EXP_AMT \r\n");
			strbuf.append("    FROM( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            SUM(ACNT) ACNT, \r\n");
			strbuf.append("            SUM(CCNT) CCNT, \r\n");
			strbuf.append("            SUM(AAMT) AAMT, \r\n");
			strbuf.append("            TRUNC (SUM(AFEE)) AFEE, \r\n");
			strbuf.append("            SUM(CAMT) CAMT, \r\n");
			strbuf.append("            TRUNC (SUM(CFEE)) CFEE, \r\n");
			strbuf.append("            SUM(ACNT)+SUM(CCNT) TOTCNT, \r\n");
			strbuf.append("            SUM(AAMT)-SUM(CAMT) TOTAMT, \r\n");
			strbuf.append("            TRUNC(SUM(AFEE)-SUM(CFEE)) TOTFEE \r\n");
			strbuf.append("        FROM( \r\n");
			strbuf.append("            SELECT \r\n");
			strbuf.append("                CASE WHEN APPGB='A' THEN COUNT(1)    ELSE 0 END ACNT, \r\n");
			strbuf.append("                CASE WHEN APPGB='C' THEN COUNT(1)    ELSE 0 END CCNT, \r\n");
			strbuf.append("                CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT, \r\n");
			strbuf.append("                CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT, \r\n");
			strbuf.append("                CASE WHEN APPGB='A' THEN SUM(FEE)    ELSE 0 END AFEE, \r\n");
			strbuf.append("                CASE WHEN APPGB='C' THEN SUM(FEE)    ELSE 0 END CFEE \r\n");
			strbuf.append("            FROM ( \r\n");
			strbuf.append("                SELECT \r\n");
			strbuf.append("                    APPGB, AMOUNT, \r\n");
			strbuf.append("                    CASE \r\n");
			strbuf.append("                        WHEN TRUNC(AMOUNT/100) < 10 THEN 10 ELSE TRUNC(AMOUNT/100)  \r\n");
			strbuf.append("                    END FEE \r\n");
			strbuf.append("                FROM \r\n");
			strbuf.append("                    GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("                WHERE SVCGB IN ('IC') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where ORG_CD='" + orgcd + "') " + set_where + " \r\n");
			strbuf.append("            ) GROUP BY APPGB \r\n");
			strbuf.append("        )  \r\n");
			strbuf.append("    ) \r\n");			
			strbuf.append(") \r\n");
			
		
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0208 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0208(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_daesa = resultList.get(5);
	
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    T1.SEQNO, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T1.APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') IFOU_APPDD, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN T1.APPGB='A' THEN '신용승인' \r\n");
			strbuf.append("        WHEN T1.APPGB='C' THEN '신용취소' \r\n");
			strbuf.append("    END IFOU_APP_AUTHSTAT, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T1.APPTM, 'HH24MISS'), 'HH24:MI:SS') IFOU_APPTM, \r\n");
			strbuf.append("    T1.OAPPDD IFOU_OAPPDD, \r\n");
			strbuf.append("    T1.APPNO IFOU_APPNO, \r\n");			
			strbuf.append("    T1.CARDNO IFOU_CARDNO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((T1.AMOUNT),'999,999,999,999,999,999')) IFOU_AMT, \r\n");
			strbuf.append("    T1.TID IFOU_TID, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T2.APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') HOSPITAL_APPDD, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN T2.APPGB='A' THEN '신용승인' \r\n");
			strbuf.append("        WHEN T2.APPGB='C' THEN '신용취소' \r\n");
			strbuf.append("    END HOSPITAL_APP_AUTHSTAT, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T2.APPTM, 'HH24MISS'), 'HH24:MI:SS') HOSPITAL_APPTM, \r\n");
			strbuf.append("    T2.OAPPDD HOSPITAL_OAPPDD, \r\n");
			strbuf.append("    T2.APPNO HOSPITAL_APPNO, \r\n");			
			strbuf.append("    T2.CARDNO HOSPITAL_CARDNO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((T2.AMOUNT),'999,999,999,999,999,999')) HOSPITAL_AMT, \r\n");
			strbuf.append("    T2.TID HOSPITAL_TID, \r\n");
			strbuf.append("    'DEASA_MSG' DEASA_MSG, \r\n");
			strbuf.append("    T1.ADD_CID MEDI_NO, \r\n");
			strbuf.append("    T1.ADD_CASHER MEDI_CD, \r\n");
			strbuf.append("    T1.MEDI_GOODS MEDI_ID, \r\n");
			strbuf.append("    T3.MCNT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("    GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("FULL OUTER JOIN( \r\n");
			strbuf.append("    SELECT CARDNO, TID,APPDD, APPTM, APPNO, AMOUNT, APPGB, OAPPDD FROM GLOB_MNG_ICVAN_BTCHK \r\n");
			strbuf.append(")T2 ON( \r\n");
			strbuf.append("    T1.APPDD=T2.APPDD \r\n");
			strbuf.append("    AND T1.APPNO=T2.APPNO \r\n");
			strbuf.append("    AND T1.AMOUNT=T2.AMOUNT \r\n");
			strbuf.append("    AND T1.APPGB=CASE WHEN T2.APPGB='A' THEN '신용승인' WHEN T2.APPGB='C' THEN '신용취소' END \r\n");
			strbuf.append("    AND T1.OAPPDD=T2.OAPPDD \r\n");
			strbuf.append(") \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT APPDD, APPNO, TID, OAPPDD, CARDNO, AMOUNT, COUNT(1) MCNT FROM GLOB_MNG_ICVAN_BTCHK WHERE authcd='0000' " + set_where + " group by APPDD, OAPPDD, TID, appno, cardno, amount \r\n");
			strbuf.append(")T3 ON( \r\n");
			strbuf.append("    T1.APPDD=T3.APPDD AND T1.APPNO=T3.APPNO AND T1.CARDNO=T3.CARDNO AND T1.AMOUNT=T3.AMOUNT \r\n");
			strbuf.append(") \r\n");
			strbuf.append("WHERE T1.authcd='0000' AND (T1.CARDNO IS NULL OR T2.CARDNO IS NULL) AND T3.MCNT<2 " + set_where_daesa + "  and T1.APPGB in ('A','C') \r\n");
			strbuf.append("ORDER BY T1.APPDD ASC, T1.APPNO ASC, T1.APPTM ASC \r\n");

			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0209 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0209(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
	
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T1.APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPTM, 'HH24MISS'), 'HH24:MI:SS') TR_APPTM,\r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN T1.APPGB='A' THEN '신용승인' \r\n");
			strbuf.append("        WHEN T1.APPGB='C' THEN '신용취소' \r\n");
			strbuf.append("    END TR_AUTHSTAT, \r\n");
			strbuf.append("    ADD_CASHER ADD_PID, \r\n");			
			strbuf.append("    ADD_CID, \r\n");
			strbuf.append("    PUR_NM TR_ACQNM, \r\n"); 
			strbuf.append("    T1.CARDNO TR_CARDNO, \r\n");
			strbuf.append("    T1.APPNO TR_APPNO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((T1.AMOUNT),'999,999,999,999,999,999')) TR_AMT, \r\n");
			strbuf.append("    HALBU TR_HALBU, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN CHECK_CARD='Y' THEN '체크카드' \r\n");
			strbuf.append("        WHEN CHECK_CARD='N' THEN '신용카드' \r\n");
			strbuf.append("        ELSE '' \r\n");
			strbuf.append("    END APP_MSG, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN T1.APPGB='C' THEN '' \r\n");
			strbuf.append("        WHEN T1.APPGB='A' THEN (SELECT C1.APPDD FROM GLOB_MNG_ICVAN C1 WHERE C1.APPGB='C' AND T1.APPDD=C1.OAPPDD AND T1.APPNO=C1.APPNO AND T1.AMOUNT=C1.AMOUNT AND T1.CARDNO=C1.CARDNO) \r\n");
			strbuf.append("    END TR_CANDD, \r\n");
			strbuf.append("    OAPPDD TR_OAPPDD, \r\n");
			strbuf.append("	   CASE \r\n");
			strbuf.append("	       WHEN CHECK_CARD='Y' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0019') \r\n");
			strbuf.append("		   ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0018') \r\n");
			strbuf.append("    END TR_CARDTP,\r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN OVSEA_CARD='Y' THEN '해외' \r\n");
			strbuf.append("        WHEN OVSEA_CARD='N' THEN '국내' \r\n");
			strbuf.append("    END OVSEA_CARD, \r\n");
			strbuf.append("    MID TR_MID, \r\n");
			strbuf.append("    ADD_RECP ADD_RHK, \r\n");
			strbuf.append("    ADD_CD ADD_PGB, \r\n");
			strbuf.append("    TID TR_TID, \r\n");
			strbuf.append("    CASE\r\n");
			strbuf.append("        WHEN RTN_CD IS NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0024') \r\n");
			strbuf.append("        WHEN RTN_CD IN('60', '67') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0025')\r\n");
			strbuf.append("        WHEN RTN_CD IN('61', '64') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0026') \r\n");
			strbuf.append("    END DP_RST_TXT,\r\n");
			strbuf.append("    TO_CHAR(TO_DATE(DEPOREQDD, 'YYYYMMDD'), 'YYYY-MM-DD')  DP_REQ_DD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(REG_DD   , 'YYYYMMDD'), 'YYYY-MM-DD')  DP_RES_DD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(REG_DD   , 'YYYYMMDD'), 'YYYY-MM-DD')  DP_REG_DD \r\n");		
			strbuf.append("FROM \r\n");
			strbuf.append("    GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT REQ_DD, REG_DD, APP_DD, APP_NO, TRANIDX, RTN_CD, CARD_NO FROM TB_MNG_DEPDATA \r\n");			
			strbuf.append(")T2 ON(T1.APPDD=T2.APP_DD AND T1.TRANIDX=T2.TRANIDX) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append(")T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append(")T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_KOCES, PUR_CD FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES OR T1.ACQ_CD=T5.PUR_CD) \r\n");
			strbuf.append("WHERE \r\n");
			strbuf.append("    (APPGB, APPDD, AMOUNT, ADD_CID, MID) IN ( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            APPGB, APPDD, AMOUNT, ADD_CID, MID \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            GLOB_MNG_ICVAN \r\n");
			strbuf.append("        WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where org_cd = '" + orgcd + "') AND OAPP_AMT IS NULL " + set_where + " \r\n");
			strbuf.append("        HAVING COUNT(1)>1 GROUP BY APPGB, APPDD, AMOUNT, ADD_CID, MID \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append("ORDER BY APPDD, APPTM DESC \r\n");

			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
	
		return jsonary;
	}
	
	/**
	 * get_sub0210 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0210(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
	
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("select \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(card_amount))                                                           ,'999,999,999,999,999,999'))  SALES_CREDIT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(check_amount))                                                          ,'999,999,999,999,999,999'))  SALES_CHECK, \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(card_amount)  + sum(check_amount))                                      ,'999,999,999,999,999,999'))  SALES_TOT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(card_fee))                                                              ,'999,999,999,999,999,999'))  FEE_CREDIT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(check_fee))                                                             ,'999,999,999,999,999,999'))  FEE_CHECK, \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(card_fee)     + sum(check_fee))                                         ,'999,999,999,999,999,999'))  FEE_TOT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(card_depo)    - sum(card_fee))                                          ,'999,999,999,999,999,999'))  DEPOSIT_CREDIT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(check_depo)   - sum(check_fee))                                         ,'999,999,999,999,999,999'))  DEPOSIT_CHECK, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((SUM(card_depo)   - SUM(card_fee))  + (SUM(check_depo) - SUM(check_fee)))   ,'999,999,999,999,999,999'))  DEPOSIT_TOT, \r\n");			
			strbuf.append("    TRIM(TO_CHAR((sum(card_amount)  - sum(card_depo))                                         ,'999,999,999,999,999,999'))  card_amount_unpaid, \r\n");
			strbuf.append("    TRIM(TO_CHAR((sum(check_amount) - sum(check_depo))                                        ,'999,999,999,999,999,999'))  check_amount_unpaid, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((sum(card_amount) - sum(card_depo)) + (sum(check_amount) - sum(check_depo))),'999,999,999,999,999,999'))  tot_amount_unpaid, \r\n");			
			strbuf.append("    S4.DEP_NM TR_DEPNM,  \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(S1.MDATE, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD \r\n");
			strbuf.append("from ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        sum(card_amount_a) - sum(card_amount_c) card_amount, \r\n");
			strbuf.append("        sum(check_amount_a) - sum(check_amount_c) check_amount, \r\n");
			strbuf.append("        sum(card_sale_amt_a) - sum(card_sale_amt_c) card_depo, \r\n");
			strbuf.append("        sum(check_sale_amt_a) - sum(check_sale_amt_c) check_depo, \r\n");
			strbuf.append("        sum(card_fee_a) - sum(card_fee_c) card_fee, \r\n");
			strbuf.append("        sum(check_fee_a) - sum(check_fee_c) check_fee, \r\n");
			strbuf.append("        MDATE, TID, ACQ_CD \r\n");
			strbuf.append("    from( \r\n");
			strbuf.append("        select \r\n");
			strbuf.append("            case when T1.APPGB = 'A' AND T1.CHECK_CARD != 'Y' then amount else 0 end card_amount_a, \r\n"); 
			strbuf.append("            case when T1.APPGB = 'C' AND T1.CHECK_CARD != 'Y' then amount else 0 end card_amount_c, \r\n");
			strbuf.append("            case when T1.APPGB = 'A' AND T1.CHECK_CARD = 'Y' then amount else 0 end check_amount_a, \r\n");
			strbuf.append("            case when T1.APPGB = 'C' AND T1.CHECK_CARD = 'Y' then amount else 0 end check_amount_c, \r\n");
			strbuf.append("            case when T2.RTN_CD = '60' AND T1.CHECK_CARD != 'Y' then sale_amt else 0 end card_sale_amt_a, \r\n");
			strbuf.append("            case when T2.RTN_CD = '67' AND T1.CHECK_CARD != 'Y' then sale_amt else 0 end card_sale_amt_c, \r\n");
			strbuf.append("            case when T2.RTN_CD = '60' AND T1.CHECK_CARD = 'Y' then sale_amt else 0 end check_sale_amt_a, \r\n");
			strbuf.append("            case when T2.RTN_CD = '67' AND T1.CHECK_CARD = 'Y' then sale_amt else 0 end check_sale_amt_c, \r\n");
			strbuf.append("            case when T2.RTN_CD = '60' AND T1.CHECK_CARD != 'Y' then fee else 0 end card_fee_a, \r\n");
			strbuf.append("            case when T2.RTN_CD = '67' AND T1.CHECK_CARD != 'Y' then fee else 0 end card_fee_c, \r\n");
			strbuf.append("            case when T2.RTN_CD = '60' AND T1.CHECK_CARD = 'Y' then fee else 0 end check_fee_a, \r\n");
			strbuf.append("            case when T2.RTN_CD = '67' AND T1.CHECK_CARD = 'Y' then fee else 0 end check_fee_c, \r\n");
			strbuf.append("            MDATE, TID, acq_cd \r\n");
			strbuf.append("        from( \r\n");
			strbuf.append("            select * \r\n");
			strbuf.append("                from GLOB_MNG_ICVAN \r\n");			
			strbuf.append("                WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where ORG_CD= '" + orgcd + "' " + depcd_where + ")" + set_where + " \r\n");
			strbuf.append("        ) T1 \r\n");
			strbuf.append("        LEFT OUTER JOIN( \r\n");
			strbuf.append("            SELECT EXP_DD, REQ_DD, REG_DD, APP_DD, TRANIDX, RSC_CD, RTN_CD, FEE, SALE_AMT \r\n");
			strbuf.append("            FROM TB_MNG_DEPDATA where RTN_CD IN ('60','67')" + set_where_dep +  "\r\n");
			strbuf.append("        ) T2 \r\n");
			strbuf.append("        ON (T1.APPDD = T2.APP_DD AND T1.TRANIDX = T2.TRANIDX) \r\n");
			strbuf.append("    ) GROUP BY mdate,tid,acq_cd) S1 \r\n");
			strbuf.append("    left outer join( \r\n");
			strbuf.append("        SELECT TERM_ID, TERM_NM, DEP_CD FROM TB_BAS_TIDMST \r\n");
			strbuf.append("    )S2 ON (S1.TID = S2.TERM_ID ) \r\n");
			strbuf.append("    left outer join( \r\n");
			strbuf.append("        select pur_nm, pur_ocd from tb_bas_purinfo \r\n");
			strbuf.append("    )S3 ON (S1.ACQ_CD= S3.pur_ocd) \r\n");
			strbuf.append("    left outer join( \r\n");
			strbuf.append("        select dep_cd, dep_nm from tb_bas_depart \r\n");
			strbuf.append("    )S4 ON (S2.dep_cd= S4.dep_cd ) \r\n");
			strbuf.append("group by S4.DEP_NM, MDATE \r\n");
			strbuf.append("order by S4.DEP_NM, MDATE \r\n");
			
			
			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * sub0301 �뜝�떙�궪�삕 Query
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-16 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0301(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
	
		try {
			strbuf = new StringBuffer();
			/*
			 * //  //  //
			 * strbuf.append("  \r\n"); // // strbuf.append(" \r\n"); //
			 * strbuf.append(" \r\n"); // strbuf.append(" \r\n"); // strbuf.append(" \r\n");
			 */			
			
			strbuf.append("SELECT \r\n");
			
			strbuf.append("    DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    T4.DEP_CD DEP_CD, \r\n");
			strbuf.append("    PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    T1.MID TR_MID, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T1.EXP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') DEP_EXP_DD, \r\n");
			strbuf.append("    TRIM(TO_CHAR((T_CNT),'999,999,999,999,999,999'))   TOT_CNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((T_BAN),'999,999,999,999,999,999'))   TOT_BAN, \r\n");
			strbuf.append("    TRIM(TO_CHAR((T_AMT),'999,999,999,999,999,999'))   TOT_AMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((T_FEE),'999,999,999,999,999,999'))   TOT_FEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR((T_EXP),'999,999,999,999,999,999'))   TOT_EXP, \r\n");
			strbuf.append("    TRIM(TO_CHAR((I_CNT),'999,999,999,999,999,999'))   DEP_CNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((I_BAN),'999,999,999,999,999,999'))   DEP_BAN, \r\n");
			strbuf.append("    TRIM(TO_CHAR((I_AMT),'999,999,999,999,999,999'))   DEP_AMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((I_FEE),'999,999,999,999,999,999'))   DEP_FEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR((I_EXP ),'999,999,999,999,999,999'))  DEP_EXP, \r\n");
			strbuf.append("    TRIM(TO_CHAR((NVL(BANK_AMT,0) ),'999,999,999,999,999,999'))  BANK_AMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((TO_NUMBER(T_EXP-I_EXP) ),'999,999,999,999,999,999'))  DIF_TOT_AMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((NVL(BANK_AMT,0)-I_EXP)),'999,999,999,999,999,999'))   DIF_BANK_AMT, \r\n");
			strbuf.append("    ACQ_CD HIDDEN \r\n");						
			strbuf.append("FROM ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        MID, EXP_DD, ACQ_CD,  \r\n");
			strbuf.append("        SUM(TOT_CNT) T_CNT, SUM(TOT_BAN) T_BAN, SUM(TOT_NETAMT) T_AMT, SUM(TOT_INPAMT) T_FEE, SUM(TOT_EXPAMT) T_EXP, \r\n");
			strbuf.append("        SUM(I_CNT) I_CNT, SUM(I_BAN) I_BAN, SUM(I_AMT) I_AMT, SUM(I_FEE) I_FEE, SUM(I_EXP) I_EXP \r\n");
			strbuf.append("    FROM ( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            MID, EXP_DD, DEP_SEQ, \r\n");
			strbuf.append("            SUM(TOT_CNT) TOT_CNT ,SUM(BAN_CNT) TOT_BAN,(SUM(EXP_AMT)+SUM(INP_AMT)) TOT_NETAMT, \r\n");
			strbuf.append("            SUM(INP_AMT) TOT_INPAMT, SUM(EXP_AMT) TOT_EXPAMT \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            TB_MNG_DEPTOT \r\n");
			strbuf.append("       WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where ORG_CD= '" + orgcd + "') " + set_where_dep + " \r\n");
			strbuf.append("       GROUP BY MID, EXP_DD, DEP_SEQ \r\n");
			strbuf.append("       ORDER BY EXP_DD DESC \r\n");
			strbuf.append("    )T1 \r\n");
			strbuf.append("    LEFT OUTER JOIN( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            DEP_SEQ, ACQ_CD,\r\n");
			strbuf.append("            (SUM(ITEM_CNT60)+SUM(ITEM_CNT67)) I_CNT, SUM(ITEM_CNTBAN) I_BAN, (SUM(ITEM_AMT60)-SUM(ITEM_AMT67)) I_AMT, \r\n");
			strbuf.append("            (SUM(ITEM_FEE60)-SUM(ITEM_FEE67)) I_FEE, (SUM(ITEM_AMT60)-SUM(ITEM_AMT67))-(SUM(ITEM_FEE60)-SUM(ITEM_FEE67)) I_EXP \r\n");
			strbuf.append("        FROM ( \r\n");
			strbuf.append("            SELECT \r\n");
			strbuf.append("                DEP_SEQ, \r\n");
			strbuf.append("                ACQ_CD, \r\n");
			strbuf.append("                CASE WHEN RTN_CD='60' THEN COUNT(1) ELSE 0 END ITEM_CNT60, \r\n");
			strbuf.append("                CASE WHEN RTN_CD='67' THEN COUNT(1) ELSE 0 END ITEM_CNT67, \r\n");
			strbuf.append("                CASE WHEN RTN_CD NOT IN ('60', '67') THEN COUNT(1) ELSE 0 END ITEM_CNTBAN, \r\n");
			strbuf.append("                CASE WHEN RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT60, \r\n");
			strbuf.append("                CASE WHEN RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT67, \r\n");
			strbuf.append("                CASE WHEN RTN_CD='60' THEN SUM(FEE) ELSE 0 END ITEM_FEE60, \r\n");
			strbuf.append("                CASE WHEN RTN_CD='67' THEN SUM(FEE) ELSE 0 END ITEM_FEE67 \r\n");
			strbuf.append("            FROM \r\n");
			strbuf.append("                TB_MNG_DEPDATA \r\n");
			strbuf.append("            WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where ORG_CD= '" + orgcd + "')  " + set_where_dep + " \r\n");
			strbuf.append("            GROUP BY DEP_SEQ, RTN_CD, ACQ_CD \r\n");
			strbuf.append("        ) \r\n");
			strbuf.append("        GROUP BY DEP_SEQ, ACQ_CD \r\n");
			strbuf.append("    )T2 ON(T1.DEP_SEQ=T2.DEP_SEQ) \r\n");
			strbuf.append("GROUP BY MID, EXP_DD, ACQ_CD \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT EXP_DD, MID, EXP_AMT BANK_AMT FROM TB_MNG_BANKDATA \r\n");
			strbuf.append("    GROUP BY EXP_DD, MID, EXP_AMT \r\n");
			strbuf.append(")T3 ON(T1.MID=T3.MID AND T1.EXP_DD=T3.EXP_DD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT ORG_CD, DEP_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO \r\n");
			strbuf.append(")T4 ON(T1.MID=T4.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT ORG_CD, ORG_NM FROM TB_BAS_ORG \r\n");
			strbuf.append(")T5 ON(T4.ORG_CD=T5.ORG_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_CD, DEP_NM FROM TB_BAS_DEPART \r\n");
			strbuf.append(")T6 ON(T4.DEP_CD=T6.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT PUR_CD, PUR_NM, PUR_SORT, PUR_KOCES,PUR_OCD FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T7 ON(T4.PUR_CD=T7.PUR_CD) \r\n");
			strbuf.append("WHERE T1.MID IN (SELECT MID FROM TB_BAS_MIDMAP where ORG_CD= '" + orgcd + "')  " + set_where_depextra  + "\r\n");
			strbuf.append("ORDER BY DEP_NM ASC, PUR_NM ASC, PUR_SORT ASC \r\n");

			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}

	            
				jsonary.add(jsonob);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0302 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0302(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
	
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    DEP_NM, \r\n");
			strbuf.append("    TERM_NM, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(T1.APP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') APP_DD, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN T7.APPGB='A' THEN '신용승인' \r\n");
			strbuf.append("        WHEN T7.APPGB='C' THEN '신용취소' \r\n");
			strbuf.append("    END APPGB, \r\n");
			strbuf.append("    PUR_NM, \r\n");
			strbuf.append("    CARD_NO, \r\n");
			strbuf.append("    T1.APP_NO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SALE_AMT),'999,999,999,999,999,999')) SALE_AMT, \r\n");
			strbuf.append("    HALBU, \r\n");
			strbuf.append("    TRIM(TO_CHAR((FEE),'999,999,999,999,999,999')) FEE,  \r\n");			
			strbuf.append("    TRIM(TO_CHAR(((SALE_AMT-FEE)),'999,999,999,999,999,999'))  EXP_AMT, \r\n");
			strbuf.append("	   CASE \r\n");
			strbuf.append("	       WHEN CHECK_CARD='Y' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0019') \r\n");
			strbuf.append("		   ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0018') \r\n");
			strbuf.append("		END TR_CARDTP,\r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN T7.OVSEA_CARD='Y' THEN '해외' \r\n");
			strbuf.append("        WHEN T7.OVSEA_CARD='N' THEN '국내' \r\n");
			strbuf.append("    END OVSEA_CARD, \r\n");
			strbuf.append("    MID, \r\n");
			strbuf.append("    TID, \r\n");
			strbuf.append("    ADD_CID,  \r\n");	
			strbuf.append("    TO_CHAR(TO_DATE(EXP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') EXP_DD, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN RTN_CD IS NULL THEN '결과없음' \r\n");
			strbuf.append("        WHEN RTN_CD IN('60', '67') THEN '정상매입' \r\n");
			strbuf.append("        WHEN RTN_CD IN('61', '64') THEN '매입반송' \r\n");
			strbuf.append("    END RTN_MSG, \r\n");			
			strbuf.append("    RTN_CD,  \r\n");	
			strbuf.append("    TO_CHAR(TO_DATE(REQ_DD, 'YYYYMMDD'), 'YYYY-MM-DD') REQ_DD  \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("    TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT ORG_CD, DEP_CD, STO_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T2 ON(T1.MID=T2.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append(")T6 ON(T1.TID=T6.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='" + orgcd + "' \r\n");
			strbuf.append(")T4 ON(T2.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT STO_NM, STO_CD, DEP_CD, ORG_CD FROM TB_BAS_STORE \r\n");
			strbuf.append(")T3 ON(T2.STO_CD=T3.STO_CD AND T2.DEP_CD=T3.DEP_CD AND T2.ORG_CD=T3.ORG_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT PUR_CD, PUR_NM FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T6 ON(T2.PUR_CD=T6.PUR_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT APPGB, APPNO, APPDD, TRANIDX, EXT_FIELD,   \r\n");			
			strbuf.append("    ADD_CID, ADD_CASHER, ADD_DATE, APPTM, OAPPDD, \r\n");
			strbuf.append("    CHECK_CARD, OVSEA_CARD FROM GLOB_MNG_ICVAN \r\n");
			strbuf.append(")T7 ON(T1.APP_NO=T7.APPNO AND T1.TRANIDX=T7.TRANIDX) \r\n");
			strbuf.append("WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where ORG_CD='" + orgcd + "')" + set_where_dep + set_where_depextra+ " \r\n");

			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0302T 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0302T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);

		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    MID, DEP_NM TR_DEPNM, PUR_NM TR_ACQNM, PUR_SORT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR(((SUM(ITEM_CNT60) + SUM(ITEM_CNT67)))                                  ,'999,999,999,999,999,999')) I_CNTTOT_CNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(ITEM_CNTBAN))                                                     ,'999,999,999,999,999,999')) I_BAN_TOT_BAN, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((SUM(ITEM_AMT60) - SUM(ITEM_AMT67)))                                  ,'999,999,999,999,999,999')) I_AMTTOT_AMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((SUM(ITEM_FEE60) - SUM(ITEM_FEE67)))                                  ,'999,999,999,999,999,999')) I_FEETOT_FEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((SUM(ITEM_AMT60) - SUM(ITEM_AMT67))-(SUM(ITEM_FEE60)-SUM(ITEM_FEE67))),'999,999,999,999,999,999')) I_EXP_TOT_EXP \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        MID, DEP_NM, PUR_NM, PUR_SORT, \r\n");
			strbuf.append("        CASE WHEN RTN_CD='60'                THEN COUNT(1) ELSE 0 END ITEM_CNT60, \r\n");
			strbuf.append("        CASE WHEN RTN_CD='67'                THEN COUNT(1) ELSE 0 END ITEM_CNT67, \r\n");
			strbuf.append("        CASE WHEN RTN_CD NOT IN ('60', '67') THEN COUNT(1) ELSE 0 END ITEM_CNTBAN, \r\n");
			strbuf.append("        CASE WHEN RTN_CD='60'                THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT60, \r\n");
			strbuf.append("        CASE WHEN RTN_CD='67'                THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT67, \r\n");
			strbuf.append("        CASE WHEN RTN_CD='60'                THEN SUM(FEE) ELSE 0 END ITEM_FEE60, \r\n");
			strbuf.append("        CASE WHEN RTN_CD='67'                THEN SUM(FEE) ELSE 0 END ITEM_FEE67 \r\n");
			strbuf.append("    FROM( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            T1.DEP_CD, CARD_NO, EXP_DD, MID, REQ_DD, TID, RTN_CD, \r\n");
			strbuf.append("            REG_DD, HALBU, SALE_AMT, RSC_CD, RS_MSG, \r\n");
			strbuf.append("            FEE, DEP_NM, PUR_NM, T6.STO_CD PUR_SORT \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("        LEFT OUTER JOIN( \r\n");
			strbuf.append("            SELECT ORG_CD, DEP_CD, STO_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO where ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append("        )T2 ON(T1.MID=T2.MER_NO) \r\n");
			strbuf.append("        LEFT OUTER JOIN( \r\n");
			strbuf.append("            SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST where ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append("        )T6 ON(T1.TID=T6.TERM_ID) \r\n");
			strbuf.append("        LEFT OUTER JOIN( \r\n");
			strbuf.append("            SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART where ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append("        )T4 ON(T2.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("        LEFT OUTER JOIN( \r\n");
			strbuf.append("            SELECT PUR_CD, PUR_NM, STO_CD FROM TB_BAS_PURINFO \r\n");
			strbuf.append("        )T6 ON(T2.PUR_CD=T6.PUR_CD) \r\n");
			strbuf.append("        LEFT OUTER JOIN( \r\n");
			strbuf.append("            SELECT APPDD, TRANIDX, EXT_FIELD,ADD_CID, CHECK_CARD, OVSEA_CARD FROM GLOB_MNG_ICVAN \r\n");
			strbuf.append("        )T7 ON(T1.APP_DD=T7.APPDD AND T1.TRANIDX=T7.TRANIDX) \r\n");
			strbuf.append("        WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where ORG_CD= '" + orgcd + "') " + set_where_dep + set_where_depextra + " \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append("    GROUP BY MID, DEP_NM, PUR_NM, PUR_SORT, RTN_CD \r\n");
			strbuf.append(") \r\n");
			strbuf.append("GROUP BY MID, DEP_NM, PUR_NM, PUR_SORT \r\n");
			strbuf.append("ORDER BY PUR_SORT ASC \r\n");

			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0303 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0303(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);

		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    T4.DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    T2.TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    T6.PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(REQ_DD, 'YYYYMMDD'), 'YYYY-MM-DD') REQ_DD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(EXP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_EXPDD, \r\n");
			strbuf.append("    TID TR_TID, \r\n");
			strbuf.append("    MID TR_MID, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ITEM_CNT),'999,999,999,999,999,999'))  ITEM_CNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ITEM_AMT),'999,999,999,999,999,999'))  ITEM_AMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ITEM_FEE),'999,999,999,999,999,999'))  ITEM_FEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ITEM_EXP),'999,999,999,999,999,999'))  ITEM_EXP, \r\n");
			// 여기 체크카드 로직 체크해야할듯? 20230426 장현석
			strbuf.append("    '0' CK_CNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((ITEM_CNT+0) ),'999,999,999,999,999,999'))  TOT_CNT, \r\n");
			strbuf.append("    '0' CK_AMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((ITEM_AMT+0)),'999,999,999,999,999,999'))   TOT_AMT, \r\n");
			strbuf.append("    '0' CK_FEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((ITEM_FEE+0)),'999,999,999,999,999,999'))   TOT_FEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((ITEM_AMT-ITEM_FEE)),'999,999,999,999,999,999'))   ITEM_SUM, \r\n");
			strbuf.append("    (0-0) CK_SUM, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((ITEM_AMT)),'999,999,999,999,999,999'))   SUM_AMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        APP_DD, \r\n");
			strbuf.append("        REQ_DD, \r\n");
			strbuf.append("        EXP_DD, \r\n");
			strbuf.append("        TID, \r\n");
			strbuf.append("        MID, \r\n");
			strbuf.append("        SUM(ITEM_CNT60)+SUM(ITEM_CNT67) ITEM_CNT, \r\n");
			strbuf.append("        SUM(ITEM_AMT60)-SUM(ITEM_AMT67) ITEM_AMT, \r\n");
			strbuf.append("        SUM(ITEM_FEE60)-SUM(ITEM_FEE67) ITEM_FEE, \r\n");
			strbuf.append("        (SUM(ITEM_AMT60)-SUM(ITEM_AMT67))-(SUM(ITEM_FEE60)-SUM(ITEM_FEE67)) ITEM_EXP \r\n");
			strbuf.append("    FROM( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            APP_DD, \r\n");
			strbuf.append("            REQ_DD, \r\n");
			strbuf.append("            EXP_DD, \r\n");
			strbuf.append("            TID, \r\n");
			strbuf.append("            MID, \r\n");
			strbuf.append("            RTN_CD, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60'                THEN COUNT(1)      ELSE 0 END ITEM_CNT60, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67'                THEN COUNT(1)      ELSE 0 END ITEM_CNT67, \r\n");
			strbuf.append("            CASE WHEN RTN_CD NOT IN ('60', '67') THEN COUNT(1)      ELSE 0 END ITEM_CNTBAN, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60'                THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT60, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67'                THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT67, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60'                THEN SUM(FEE)      ELSE 0 END ITEM_FEE60, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67'                THEN SUM(FEE)      ELSE 0 END ITEM_FEE67 \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("        WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where ORG_CD= '" + orgcd  + "'" + depcd_where  + ") " + set_where_dep +  set_where_depextra +" \r\n");
			strbuf.append("        GROUP BY APP_DD, REQ_DD, EXP_DD, MID, TID, RTN_CD \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append("GROUP BY APP_DD, REQ_DD, EXP_DD, TID, MID \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT TERM_ID, TERM_NM FROM TB_BAS_TIDMST WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T2 ON(T1.TID=T2.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT ORG_CD, DEP_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T3 ON(T1.MID=T3.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT ORG_CD, ORG_NM FROM TB_BAS_ORG \r\n");
			strbuf.append(")T5 ON(T3.ORG_CD=T5.ORG_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_CD, DEP_NM FROM TB_BAS_DEPART WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT PUR_CD, PUR_NM, PUR_SORT, PUR_KOCES,PUR_OCD FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T6 ON(T3.PUR_CD=T6.PUR_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT ORG_CD, USER_PUR_CD, USER_PURSORT FROM TB_BAS_USERPURINFO WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")S3 ON(T6.PUR_CD=S3.USER_PUR_CD) \r\n");
			strbuf.append("WHERE ITEM_CNT>0 \r\n");
			strbuf.append("ORDER BY T3.DEP_CD ASC, T1.APP_DD ASC, T6.PUR_NM ASC, T1.REQ_DD ASC, T1.EXP_DD ASC \r\n");

			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0303T 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0303T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT  \r\n");
			strbuf.append("    T2.TERM_NM TR_TIDNM,  \r\n");
			strbuf.append("    TID TR_TID,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((BC)    ,'999,999,999,999,999,999'))  BC,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((NH)    ,'999,999,999,999,999,999'))  NH,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((KB)    ,'999,999,999,999,999,999'))  KB,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SS)    ,'999,999,999,999,999,999'))  SS,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((HN)    ,'999,999,999,999,999,999'))  HN,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((LO)    ,'999,999,999,999,999,999'))  LO,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((HD)    ,'999,999,999,999,999,999'))  HD,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SI)    ,'999,999,999,999,999,999'))  SI,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((BCF)   ,'999,999,999,999,999,999'))  BCF,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((NHF)   ,'999,999,999,999,999,999'))  NHF,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((KBF)   ,'999,999,999,999,999,999'))  KBF,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SSF)   ,'999,999,999,999,999,999'))  SSF,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((HNF)   ,'999,999,999,999,999,999'))  HNF,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((LOF)   ,'999,999,999,999,999,999'))  LOF,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((HDF)   ,'999,999,999,999,999,999'))  HDF,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SIF)   ,'999,999,999,999,999,999'))  SIF,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((JH)    ,'999,999,999,999,999,999'))  JH, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AP)    ,'999,999,999,999,999,999'))  AP, \r\n");
			strbuf.append("    TRIM(TO_CHAR((WP)    ,'999,999,999,999,999,999'))  WP, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ZERO)  ,'999,999,999,999,999,999'))  ZERO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((KAKAO) ,'999,999,999,999,999,999'))  KAKAO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((JHF)   ,'999,999,999,999,999,999'))  JHF, \r\n");
			strbuf.append("    TRIM(TO_CHAR((APF)   ,'999,999,999,999,999,999'))  APF, \r\n");
			strbuf.append("    TRIM(TO_CHAR((WPF)   ,'999,999,999,999,999,999'))  WPF, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ZEROF) ,'999,999,999,999,999,999'))  ZEROF, \r\n");
			strbuf.append("    TRIM(TO_CHAR((KAKAOF),'999,999,999,999,999,999'))  KAKAOF \r\n");
			strbuf.append("FROM (  \r\n");
			strbuf.append("    SELECT  \r\n");
			strbuf.append("        TID,  \r\n");
			strbuf.append("        SUM(BCA)-SUM(BCC) BC,  \r\n");
			strbuf.append("        SUM(NHA)-SUM(NHC) NH,  \r\n");
			strbuf.append("        SUM(KBA)-SUM(KBC) KB,  \r\n");
			strbuf.append("        SUM(SSA)-SUM(SSC) SS,  \r\n");
			strbuf.append("        SUM(HNA)-SUM(HNC) HN,  \r\n");
			strbuf.append("        SUM(LOA)-SUM(LOC) LO,  \r\n");
			strbuf.append("        SUM(HDA)-SUM(HDC) HD,  \r\n");
			strbuf.append("        SUM(SIA)-SUM(SIC) SI,  \r\n");
			strbuf.append("        SUM(BCAF)-SUM(BCCF) BCF,  \r\n");
			strbuf.append("        SUM(NHAF)-SUM(NHCF) NHF,  \r\n");
			strbuf.append("        SUM(KBAF)-SUM(KBCF) KBF,  \r\n");
			strbuf.append("        SUM(SSAF)-SUM(SSCF) SSF,  \r\n");
			strbuf.append("        SUM(HNAF)-SUM(HNCF) HNF,  \r\n");
			strbuf.append("        SUM(LOAF)-SUM(LOCF) LOF,  \r\n");
			strbuf.append("        SUM(HDAF)-SUM(HDCF) HDF,  \r\n");
			strbuf.append("        SUM(SIAF)-SUM(SICF) SIF,  \r\n");
			strbuf.append("        SUM(JHA)-SUM(JHC) JH, \r\n");
			strbuf.append("        SUM(APA)-SUM(APC) AP, \r\n");
			strbuf.append("        SUM(WPA)-SUM(WPC) WP, \r\n");
			strbuf.append("        SUM(ZEROA)-SUM(ZEROC) ZERO, \r\n");
			strbuf.append("        SUM(KAKAOA)-SUM(KAKAOC) KAKAO, \r\n");
			strbuf.append("        SUM(JHAF)-SUM(JHCF) JHF,  \r\n");
			strbuf.append("        SUM(APAF)-SUM(APCF) APF, \r\n");
			strbuf.append("        SUM(WPAF)-SUM(WPCF) WPF,  \r\n");
			strbuf.append("        SUM(ZEROAF)-SUM(ZEROCF) ZEROF,  \r\n");
			strbuf.append("        SUM(KAKAOAF)-SUM(KAKAOCF) KAKAOF  \r\n");
			strbuf.append("    FROM(  \r\n");
			strbuf.append("        SELECT  \r\n");
			strbuf.append("            TID,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END BCA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END NHA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END KBA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END SSA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END HNA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END LOA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END HDA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END SIA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END BCC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END NHC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END KBC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END SSC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END HNC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END LOC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END HDC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END SIC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END BCAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END NHAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END KBAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END SSAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END HNAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END LOAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END HDAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END SIAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END BCCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END NHCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END KBCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END SSCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END HNCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END LOCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END HDCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END SICF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END JHA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END JHC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END JHAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END JHCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END APA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END APC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END APAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END APCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END WPA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END WPC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END WPAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END WPCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END ZEROA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END ZEROC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END ZEROAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END ZEROCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END KAKAOA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END KAKAOC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END KAKAOAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END KAKAOCF  \r\n");
			strbuf.append("        FROM  \r\n");
			strbuf.append("            TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("        WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where " + "ORG_CD= '" + orgcd + "'" + depcd_where  + ") " + set_where_dep + set_where_depextra + " \r\n");
			strbuf.append("        GROUP BY TID, ACQ_CD, RTN_CD  \r\n");
			strbuf.append("    ) GROUP BY TID  \r\n");
			strbuf.append(") T1  \r\n");
			strbuf.append("LEFT OUTER JOIN(  \r\n");
			strbuf.append("    SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST  \r\n");
			strbuf.append(")T2 ON(T1.TID=T2.TERM_ID)  \r\n");


			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());

			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0304 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0304(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");			
			strbuf.append("    T3.DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    T5.TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APP_DD, 'YYYYMMDD'), 'YYYY-MM-DD')  TR_APPDD, \r\n");
			strbuf.append("    CASE   \r\n");
			strbuf.append("        WHEN RTN_CD IS NULL THEN '결과없음' \r\n");
			strbuf.append("        WHEN RTN_CD IN('60', '67') THEN '정상매입' \r\n");
			strbuf.append("        WHEN RTN_CD IN('61', '64') THEN '매입반송' \r\n");
			strbuf.append("    END TR_AUTHSTAT, \r\n");
			strbuf.append("    T6.PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    CARD_NO TR_CARDNO, \r\n");
			strbuf.append("    APP_NO TR_APPNO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SALE_AMT),'999,999,999,999,999,999'))   TR_AMT, \r\n");
			strbuf.append("    HALBU TR_HALBU, \r\n");
			strbuf.append("    TRIM(TO_CHAR((FEE),'999,999,999,999,999,999'))   DP_FEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SALE_AMT-FEE),'999,999,999,999,999,999'))   DP_EXP_AMT, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN CHECK_CARD='Y' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0019') \r\n");
			strbuf.append("        ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='" + orgcd + "' AND SCD_CD='SCD0018') \r\n");
			strbuf.append("    END TR_CARDTP, \r\n");	
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN OVSEA_CARD='Y' THEN '해외' \r\n");
			strbuf.append("        WHEN OVSEA_CARD='N' THEN '국내' \r\n");
			strbuf.append("    END OVERSEA_CARD, \r\n");
			strbuf.append("    MID TR_MID, \r\n");
			strbuf.append("    TID TR_TID, \r\n");
			strbuf.append("    ADD_CID ADD_PID, \r\n"); 
			strbuf.append("    TO_CHAR(TO_DATE(EXP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') DP_EXP_DD, \r\n");
			strbuf.append("    CASE \r\n");
			strbuf.append("        WHEN RTN_CD IS NULL THEN '결과없음' \r\n");
			strbuf.append("        WHEN RTN_CD IN('60', '67') THEN '정상매입' \r\n");
			strbuf.append("        WHEN RTN_CD IN('61', '64') THEN '매입반송' \r\n");
			strbuf.append("    END DP_RST_TXT, \r\n");	
			strbuf.append("    RTN_CD DP_DEPO,  \r\n");	
			strbuf.append("    TO_CHAR(TO_DATE(REG_DD, 'YYYYMMDD'), 'YYYY-MM-DD') DP_REG_DD \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT ORG_CD, DEP_CD, STO_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T2 ON(T1.MID=T2.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T3 ON(T2.DEP_CD=T3.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT STO_NM, STO_CD, DEP_CD, ORG_CD FROM TB_BAS_STORE \r\n");
			strbuf.append(")T4 ON(T2.STO_CD=T4.STO_CD AND T2.DEP_CD=T4.DEP_CD AND T2.ORG_CD=T4.ORG_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T5 ON(T1.TID=T5.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT PUR_CD, PUR_NM FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T6 ON(T2.PUR_CD=T6.PUR_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT CHECK_CARD, OVSEA_CARD, TRANIDX, ADD_CID FROM GLOB_MNG_ICVAN \r\n");
			strbuf.append("WHERE SVCGB IN ('CC', 'CE')  AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where ORG_CD='" + orgcd + "' " + depcd_where + ") " + set_where + "\r\n");
			strbuf.append(")T7 ON(T1.TRANIDX=T7.TRANIDX) \r\n");
			strbuf.append("WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where ORG_CD= '" + orgcd + "') " + set_where_dep + set_where_depextra +" \r\n");

			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * get_sub0304T 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0304T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    T2.TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TID TR_TID, \r\n");
			strbuf.append("    TRIM(TO_CHAR((BC),'999,999,999,999,999,999'))  BC, \r\n");
			strbuf.append("    TRIM(TO_CHAR((NH),'999,999,999,999,999,999'))  NH, \r\n");
			strbuf.append("    TRIM(TO_CHAR((KB),'999,999,999,999,999,999'))  KB, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SS),'999,999,999,999,999,999'))  SS, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HN),'999,999,999,999,999,999'))  HN, \r\n");
			strbuf.append("    TRIM(TO_CHAR((LO),'999,999,999,999,999,999'))  LO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HD),'999,999,999,999,999,999'))  HD, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SI),'999,999,999,999,999,999'))  SI, \r\n");
			strbuf.append("    TRIM(TO_CHAR((JH),'999,999,999,999,999,999'))  JH, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AP),'999,999,999,999,999,999'))  AP, \r\n");
			strbuf.append("    TRIM(TO_CHAR((WP),'999,999,999,999,999,999'))  WP, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ZERO),'999,999,999,999,999,999'))  ZERO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((KAKAO),'999,999,999,999,999,999'))  KAKAO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((JHF),'999,999,999,999,999,999'))  JHF, \r\n");
			strbuf.append("    TRIM(TO_CHAR((APF),'999,999,999,999,999,999'))  APF, \r\n");
			strbuf.append("    TRIM(TO_CHAR((WPF),'999,999,999,999,999,999'))  WPF, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ZEROF),'999,999,999,999,999,999'))  ZEROF, \r\n");
			strbuf.append("    TRIM(TO_CHAR((KAKAOF),'999,999,999,999,999,999'))  KAKAOF \r\n");
			strbuf.append("FROM ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        TID, \r\n");
			strbuf.append("        SUM(BCA)    - SUM(BCC)     BC, \r\n");
			strbuf.append("        SUM(NHA)    - SUM(NHC)     NH, \r\n");
			strbuf.append("        SUM(KBA)    - SUM(KBC)     KB, \r\n");
			strbuf.append("        SUM(SSA)    - SUM(SSC)     SS, \r\n");
			strbuf.append("        SUM(HNA)    - SUM(HNC)     HN, \r\n");
			strbuf.append("        SUM(LOA)    - SUM(LOC)     LO, \r\n");
			strbuf.append("        SUM(HDA)    - SUM(HDC)     HD, \r\n");
			strbuf.append("        SUM(SIA)    - SUM(SIC)     SI, \r\n");
			strbuf.append("        SUM(JHA)    - SUM(JHC)     JH, \r\n");
			strbuf.append("        SUM(APA)    - SUM(APC)     AP, \r\n");
			strbuf.append("        SUM(WPA)    - SUM(WPC)     WP, \r\n");
			strbuf.append("        SUM(ZEROA)  - SUM(ZEROC)   ZERO, \r\n");
			strbuf.append("        SUM(KAKAOA) - SUM(KAKAOC)  KAKAO, \r\n");
			strbuf.append("        SUM(JHAF)   - SUM(JHCF)    JHF,  \r\n");
			strbuf.append("        SUM(APAF)   - SUM(APCF)    APF, \r\n");
			strbuf.append("        SUM(WPAF)   - SUM(WPCF)    WPF,  \r\n");
			strbuf.append("        SUM(ZEROAF) - SUM(ZEROCF)  ZEROF,  \r\n");
			strbuf.append("        SUM(KAKAOAF)- SUM(KAKAOCF) KAKAOF  \r\n");
			strbuf.append("    FROM ( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            TID, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END BCA, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END NHA, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END KBA, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END SSA, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END HNA, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END LOA, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END HDA, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END SIA, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END BCC, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END NHC, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END KBC, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END SSC, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END HNC, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END LOC, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END HDC, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END SIC, \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END JHA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END JHC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END JHAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END JHCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END APA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END APC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END APAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END APCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END WPA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END WPC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END WPAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END WPCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END ZEROA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END ZEROC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END ZEROAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END ZEROCF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END KAKAOA,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END KAKAOC,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='60' THEN SUM(FEE)      ELSE 0 END KAKAOAF,  \r\n");
			strbuf.append("            CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='67' THEN SUM(FEE)      ELSE 0 END KAKAOCF  \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("        WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where ORG_CD= '" + orgcd + "') " + set_where_dep + set_where_depextra + " \r\n");
			strbuf.append("        GROUP BY TID, ACQ_CD, RTN_CD \r\n");	
			strbuf.append("    ) GROUP BY TID \r\n");
			strbuf.append(") T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST \r\n");
			strbuf.append(")T2 ON(T1.TID=T2.TERM_ID) \r\n");
//			strbuf.append("LEFT OUTER JOIN( \r\n");
//			strbuf.append("SELECT CHECK_CARD, OVSEA_CARD, TRANIDX, ADD_CID FROM GLOB_MNG_ICVAN \r\n");
//			strbuf.append("WHERE SVCGB IN ('CC', 'CE')  AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where ORG_CD='" + orgcd + "' " + depcd_where + ") " + set_where + "\r\n");
//			strbuf.append(")T3 ON(T1.TRANIDX=T3.TRANIDX) \r\n");
		

			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0305
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0305(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    '보류' DELEY, \r\n");
			strbuf.append("    DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPDD, 'YYYYMMDD'), 'YYYY-MM-DD')  TR_APPDD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPTM, 'HH24MISS'), 'HH24:MI:SS')  TR_APPTM, \r\n");
			strbuf.append("    AUTHMSG TR_AUTHSTAT, \r\n");
			strbuf.append("    ADD_CID ADD_PID, \r\n");
			strbuf.append("    ADD_CASHER, \r\n");
			strbuf.append("    PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    CARDNO TR_CARDNO, \r\n");
			strbuf.append("    APPNO TR_APPNO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AMOUNT),'999,999,999,999,999,999')) TR_AMT, \r\n");
			strbuf.append("    HALBU TR_HALBU, \r\n");
			strbuf.append("    APPGB_TXT APP_MSG, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(TR_CANDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_CANDD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(OAPPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_OAPPDD, \r\n");
			strbuf.append("    CARDTP_TXT TR_CARDTP, \r\n");
			strbuf.append("    OVSEA_CARD, \r\n");
			strbuf.append("    MID TR_MID, \r\n");
			strbuf.append("    ADD_CD ADD_RHK, \r\n");
			strbuf.append("    ADD_GB ADD_PGB, \r\n");
			strbuf.append("    TID TR_TID, \r\n");
			strbuf.append("    TRAN_STAT DEP_STAT \r\n");
			strbuf.append("FROM ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        DEP_NM, TERM_NM, APPDD, APPTM, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='A' AND TSTAT IS NULL THEN '승인거래' \r\n");
			strbuf.append("            WHEN APPGB='A' AND TSTAT=APPDD THEN '당일취소' \r\n");
			strbuf.append("            WHEN APPGB='C' AND APPDD=OAPPDD THEN '당일취소' \r\n");
			strbuf.append("            WHEN APPGB='C' AND APPDD<>OAPPDD THEN '전일취소' \r\n");
			strbuf.append("            WHEN APPGB='A' AND APPDD<>TSTAT AND TSTAT IS NOT NULL THEN '전일취소' \r\n");
			strbuf.append("        END AUTHMSG, \r\n");
			strbuf.append("        ADD_CID, ADD_CASHER, PUR_NM, CARDNO, APPNO,   \r\n");
			strbuf.append("        AMOUNT, HALBU, \r\n");			
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='A' THEN '신용승인' \r\n");
			strbuf.append("            WHEN APPGB='C' THEN '신용취소' \r\n");
			strbuf.append("        END APPGB_TXT, \r\n");			
			strbuf.append("        TSTAT TR_CANDD,  OAPPDD, \r\n");
			strbuf.append("        CASE  \r\n");
			strbuf.append("            WHEN CHECK_CARD='Y' THEN '체크카드' ELSE '신용카드' \r\n");
			strbuf.append("        END CARDTP_TXT,  \r\n");			
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN OVSEA_CARD='Y' THEN '해외' \r\n");
			strbuf.append("            WHEN OVSEA_CARD='N' THEN '국내' \r\n");
			strbuf.append("        END OVSEA_CARD, \r\n");
			strbuf.append("        MID, ADD_CD, ADD_GB, TID, TRAN_STAT \r\n");
			strbuf.append("    FROM( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            DEP_NM, TERM_NM, T1.APPDD, APPTM, T1.APPGB, \r\n");
			strbuf.append("            ADD_CID, ADD_CASHER, PUR_NM, T1.CARDNO, T1.APPNO, \r\n");
			strbuf.append("            T1.AMOUNT, HALBU, \r\n");
			strbuf.append("            CASE \r\n");
			strbuf.append("                WHEN APPGB='C' THEN '' \r\n");
			strbuf.append("                WHEN APPGB='A' THEN (SELECT MAX(C1.APPDD) FROM GLOB_MNG_ICVAN C1 WHERE C1.APPGB='C' AND T1.APPDD=C1.OAPPDD AND T1.APPNO=C1.APPNO AND T1.AMOUNT=C1.AMOUNT AND T1.CARDNO=C1.CARDNO) \r\n");
			strbuf.append("            END TSTAT,  \r\n");
			strbuf.append("            OAPPDD, CHECK_CARD, OVSEA_CARD, MID,  \r\n");
			strbuf.append("            ADD_CD, ADD_GB, TID, NVL(TRAN_STAT, 'TR00') TRAN_STAT  \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("        LEFT OUTER JOIN(SELECT EXP_DD, REQ_DD, REG_DD, APP_DD, TRANIDX, RSC_CD, RTN_CD FROM TB_MNG_DEPDATA \r\n");
			strbuf.append("        )T2 ON(T1.APPDD=T2.APP_DD AND T1.TRANIDX=T2.TRANIDX) \r\n");
			strbuf.append("        LEFT OUTER JOIN( SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD= '" + orgcd + "')T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("        LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD= '" + orgcd + "')T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("        LEFT OUTER JOIN( SELECT PUR_NM, PUR_CD, PUR_OCD, PUR_KOCES FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES OR T1.ACQ_CD=T5.PUR_CD) \r\n");
			strbuf.append("        WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND MID IN ( \r\n");
			strbuf.append("            SELECT MID FROM TB_BAS_MIDMAP MT1 \r\n");
			strbuf.append("            LEFT OUTER JOIN( \r\n");
			strbuf.append("                SELECT MER_NO, MTYPE FROM TB_BAS_MERINFO WHERE ORG_CD= '" + orgcd + "' " + depcd_where + " \r\n");
			strbuf.append("            )MT2 ON(MT1.MID=MT2.MER_NO) \r\n");
			strbuf.append("            WHERE MT1.ORG_CD= '" + orgcd + "'  \r\n");
			strbuf.append("        ) and NVL(TRAN_STAT,'TR00') IN ('TR00','RV01','DP99') " + set_where + " \r\n");
			strbuf.append("        order by APPDD desc, apptm desc \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append(") \r\n");
			

			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0305T 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0305T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    TR_ACQNM, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(TOTCNT))    ,'999,999,999,999,999,999'))  TOTCNT,	\r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(TOTAMT))    ,'999,999,999,999,999,999'))  TOTAMT,	\r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(ACNT))      ,'999,999,999,999,999,999'))  ACNT,    \r\n");			
			strbuf.append("    TRIM(TO_CHAR((SUM(AAMT))      ,'999,999,999,999,999,999'))  AAMT,	\r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CCNT))      ,'999,999,999,999,999,999'))  CCNT,	\r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CAMT))      ,'999,999,999,999,999,999'))  CAMT,	\r\n");			
			strbuf.append("    TRIM(TO_CHAR((SUM(DELEY_ACNT)),'999,999,999,999,999,999'))  DELEY_ACNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(DELEY_AAMT)),'999,999,999,999,999,999'))  DELEY_AAMT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(DELEY_CCNT)),'999,999,999,999,999,999'))  DELEY_CCNT,  \r\n");		
			strbuf.append("    TRIM(TO_CHAR((SUM(DELEY_CAMT)),'999,999,999,999,999,999'))  DELEY_CAMT,  \r\n");		
			strbuf.append("    MID  \r\n");				
			strbuf.append("FROM ( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("    PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    (ACNT+CCNT) TOTCNT,	\r\n");
			strbuf.append("    (AAMT-CAMT) TOTAMT,	\r\n");
			strbuf.append("    ACNT, AAMT, CCNT, CAMT, \r\n");
			strbuf.append("    RACNT DELEY_ACNT,  \r\n");
			strbuf.append("    RAAMT DELEY_AAMT,  \r\n");
			strbuf.append("    RCCNT DELEY_CCNT,  \r\n");		
			strbuf.append("    RCAMT DELEY_CAMT,  \r\n");		
			strbuf.append("    TT1.MID  \r\n");				
			strbuf.append("FROM ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        MID, APPNO, APPDD, TRANIDX, SUM(ACNT) ACNT, SUM(CCNT) CCNT, SUM(AAMT) AAMT, SUM(CAMT) CAMT, SUM(RACNT) RACNT, SUM(RCCNT) RCCNT, SUM(RAAMT) RAAMT, SUM(RCAMT) RCAMT \r\n");
			strbuf.append("    FROM( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            MID, APPNO, TRANIDX, APPDD, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND TRAN_STAT IN ('TR00', 'DP99' ,'RQOO') THEN COUNT(1)    ELSE 0 END ACNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND TRAN_STAT IN ('TR00', 'DP99' ,'RQOO') THEN SUM(AMOUNT) ELSE 0 END AAMT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND TRAN_STAT IN ('TR00', 'DP99' ,'RQOO') THEN COUNT(1)    ELSE 0 END CCNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND TRAN_STAT IN ('TR00', 'DP99' ,'RQOO') THEN SUM(AMOUNT) ELSE 0 END CAMT, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND TRAN_STAT IN ('RV01')                 THEN COUNT(1)    ELSE 0 END RACNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='A' AND TRAN_STAT IN ('RV01')                 THEN SUM(AMOUNT) ELSE 0 END RAAMT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND TRAN_STAT IN ('RV01')                 THEN COUNT(1)    ELSE 0 END RCCNT, \r\n");
			strbuf.append("            CASE WHEN APPGB='C' AND TRAN_STAT IN ('RV01')                 THEN SUM(AMOUNT) ELSE 0 END RCAMT \r\n");
			strbuf.append("        FROM( \r\n");
			strbuf.append("            SELECT \r\n");
			strbuf.append("                TID, MID, APPGB, AMOUNT, TRAN_STAT, APPNO , TRANIDX, APPDD \r\n");
			strbuf.append("            FROM( \r\n");
			strbuf.append("                SELECT \r\n");
			strbuf.append("                    TID, MID, APPGB, AMOUNT, TRAN_STAT, APPNO, TRANIDX, APPDD \r\n");
			strbuf.append("                FROM( \r\n");
			strbuf.append("                    SELECT \r\n");
			strbuf.append("                        TID, MID, APPGB,  APPNO, TRANIDX, APPDD,  \r\n");
			strbuf.append("                        T1.AMOUNT,  \r\n");
			strbuf.append("                        NVL(TRAN_STAT, 'TR00') TRAN_STAT \r\n");
			strbuf.append("                    FROM \r\n");
			strbuf.append("                        GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("                    WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND MID IN ( \r\n");
			strbuf.append("                        SELECT MID FROM TB_BAS_MIDMAP MT1 \r\n");
			strbuf.append("                        LEFT OUTER JOIN( \r\n");
			strbuf.append("                            SELECT MER_NO, MTYPE FROM TB_BAS_MERINFO WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append("                        )MT2 ON(MT1.MID=MT2.MER_NO) \r\n");
			strbuf.append("                    WHERE MT1.ORG_CD= '" + orgcd + "'  \r\n");
			strbuf.append("                    ) and NVL(TRAN_STAT,'TR00') IN ('TR00','RV01','DP99') " + set_where + " \r\n");
			strbuf.append("                    order by APPDD desc, apptm desc \r\n");
			strbuf.append("                ) \r\n");
			strbuf.append("            ) \r\n");
			strbuf.append("        ) GROUP BY TID,MID, APPGB, TRAN_STAT, APPNO, APPDD, TRANIDX   \r\n");
			strbuf.append("    ) GROUP BY MID, APPNO,APPDD, TRANIDX \r\n"); 
			strbuf.append(") TT1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT ORG_CD, DEP_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO WHERE ORG_CD= '" + orgcd +  "'" + depcd_where  + " \r\n");
			strbuf.append(")T3 ON(TT1.MID=T3.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT PUR_CD, PUR_NM, PUR_SORT, PUR_KOCES FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T6 ON(T3.PUR_CD=T6.PUR_CD) \r\n");			
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT RTN_CD, TRANIDX, APP_DD, APP_NO FROM TB_MNG_DEPDATA \r\n");
			strbuf.append(")T7 ON(TT1.TRANIDX=T7.TRANIDX AND TT1.APPDD = T7.APP_DD AND TT1.APPNO = T7.APP_NO) \r\n");						
			strbuf.append("ORDER BY PUR_SORT ASC \r\n");
			strbuf.append("    ) GROUP BY MID, TR_ACQNM \r\n"); 			
			
//			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * get_sub0306 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0306(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_req = resultList.get(4);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    to_char(regdate, 'YYYY-MM-DD') REQ_DD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(SDATE, 'YYYYMMDD'), 'YYYY-MM-DD') SDATE, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(EDATE, 'YYYYMMDD'), 'YYYY-MM-DD') EDATE, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((ACNT+CCNT+HACNT+HCCNT)),'999,999,999,999,999,999'))  TOTCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((((AAMT+HAAMT)-(CAMT+HCAMT))),'999,999,999,999,999,999'))  TOTAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT),'999,999,999,999,999,999')) ACNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT),'999,999,999,999,999,999')) AAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((CCNT),'999,999,999,999,999,999')) CCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((CAMT),'999,999,999,999,999,999')) CAMT, \r\n");
			strbuf.append("    USERID REQ_ID, \r\n");
			strbuf.append("    DEPOSEQ, ORGCD,  BIZNO,   \r\n");
			strbuf.append("    TRIM(TO_CHAR(((ACNT+CCNT)),'999,999,999,999,999,999'))  NORCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((AAMT-CAMT)),'999,999,999,999,999,999'))  NORAMT,   \r\n");	
			strbuf.append("    TRIM(TO_CHAR((HACNT),'999,999,999,999,999,999')) HACNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HAAMT),'999,999,999,999,999,999')) HAAMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HCCNT),'999,999,999,999,999,999')) HCCNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HCAMT),'999,999,999,999,999,999')) HCAMT, \r\n");			
			strbuf.append("    TRIM(TO_CHAR(((HACNT+HCCNT)),'999,999,999,999,999,999'))  HALBUCNT,  \r\n");
			strbuf.append("    TRIM(TO_CHAR(((HAAMT-HCAMT)),'999,999,999,999,999,999'))  HALBUAMT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_HIS_DPREQ_TOT \r\n");
			strbuf.append("WHERE ORGCD = '" + orgcd + "' " + set_where_req + " \r\n");
			strbuf.append("order by REQ_DD desc \r\n");

			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0307
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0307(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			
			
			strbuf.append("SELECT \r\n");
			strbuf.append("    DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APPDD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APPDD, \r\n");
			strbuf.append("    TSTAT_TXT TR_AUTHSTAT, \r\n");
			strbuf.append("    ADD_CID ADD_PID, \r\n");
			strbuf.append("    PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    CARDNO TR_CARDNO, \r\n");
			strbuf.append("    APPNO TR_APPNO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((AMOUNT),'999,999,999,999,999,999')) TR_AMT, \r\n");
			strbuf.append("    HALBU TR_HALBU, \r\n");
			strbuf.append("    APP_MSG, \r\n");
			strbuf.append("    TSTAT TR_CANDD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(OAPPDD, 'YYYYMMDD'), 'YYYY-MM-DD')  TR_OAPPDD, \r\n");
			strbuf.append("    CARDTP_TXT TR_CARDTP, \r\n");
			strbuf.append("    OVSEA_CARD TR_OVSEA_CARD, \r\n");
			strbuf.append("    MID TR_MID, \r\n");
			strbuf.append("    ADD_CD ADD_RHK, \r\n");
			strbuf.append("    ADD_GB ADD_PGB, \r\n");
			strbuf.append("    TID TR_TID, \r\n");
			strbuf.append("    RTN_TXT DEP_STAT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        DEP_NM, TERM_NM, APPDD, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='A' AND TSTAT IS NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0011') \r\n");
			strbuf.append("            WHEN APPGB='A' AND TSTAT=APPDD THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0012') \r\n");
			strbuf.append("            WHEN APPGB='C' AND APPDD=OAPPDD THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd +"' AND SCD_CD='SCD0012') \r\n");
			strbuf.append("            WHEN APPGB='C' AND APPDD<>OAPPDD THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0013') \r\n");
			strbuf.append("            WHEN APPGB='A' AND APPDD<>TSTAT AND TSTAT IS NOT NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0013') \r\n");
			strbuf.append("        END TSTAT_TXT, \r\n");
			strbuf.append("        ADD_CID, PUR_NM, CARDNO, APPNO, AMOUNT,\r\n");			
			strbuf.append("        HALBU, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN APPGB='A' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0015') \r\n");
			strbuf.append("            WHEN APPGB='C' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0016') \r\n");
			strbuf.append("        END APP_MSG, \r\n");	
			strbuf.append("        TSTAT, \r\n");
			strbuf.append("        OAPPDD, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN CHECK_CARD='Y' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0019') \r\n");
			strbuf.append("            ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0018')  \r\n");
			strbuf.append("        END CARDTP_TXT, \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN OVSEA_CARD='Y' THEN '해외' \r\n");
			strbuf.append("            WHEN OVSEA_CARD='N' THEN '국내' \r\n");
			strbuf.append("        END OVSEA_CARD, \r\n");
			strbuf.append("        MID, ADD_CD,  \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN ADD_GB IN ('1', 'O') THEN 'O' \r\n");
			strbuf.append("            WHEN ADD_GB IN ('2', 'E') THEN 'E' \r\n");
			strbuf.append("            WHEN ADD_GB IN ('3', 'I') THEN 'I' \r\n");
			strbuf.append("            WHEN ADD_GB IN ('4', 'G') THEN 'G' \r\n");
			strbuf.append("            WHEN ADD_GB='5' THEN 'ADDGB5' \r\n");
			strbuf.append("            WHEN ADD_GB='6' THEN 'ADDGB6' \r\n");
			strbuf.append("            ELSE '' \r\n");
			strbuf.append("        END ADD_GB, \r\n");
			strbuf.append("        TID,    \r\n");
			strbuf.append("        CASE \r\n");
			strbuf.append("            WHEN RTN_CD IS NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0024') \r\n");
			strbuf.append("            WHEN RTN_CD IN('60', '67') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0025') \r\n");
			strbuf.append("            WHEN RTN_CD IN('61', '64') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD= '" + orgcd + "' AND SCD_CD='SCD0026') \r\n");
			strbuf.append("        END RTN_TXT \r\n");			
			strbuf.append("    FROM ( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            DEP_NM, TERM_NM, T1.APPDD, T1.APPGB, ADD_CID, \r\n");
			strbuf.append("            PUR_NM, T1.CARDNO, T1.APPNO, T1.AMOUNT, HALBU, \r\n");
			strbuf.append("            OAPPDD, CHECK_CARD, OVSEA_CARD, MID,  ADD_CD,\r\n");
			strbuf.append("            ADD_GB, TID, RTN_CD, \r\n");
			strbuf.append("            CASE \r\n");
			strbuf.append("                WHEN APPGB='C' THEN '' \r\n");
			strbuf.append("                WHEN APPGB='A' THEN (SELECT MAX(C1.APPDD) FROM GLOB_MNG_ICVAN C1 WHERE C1.APPGB='C' AND T1.APPDD=C1.OAPPDD AND T1.APPNO=C1.APPNO AND T1.AMOUNT=C1.AMOUNT AND T1.CARDNO=C1.CARDNO) \r\n");
			strbuf.append("            END TSTAT \r\n");
			strbuf.append("        FROM \r\n");
			strbuf.append("            GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("        LEFT OUTER JOIN ( \r\n");
			strbuf.append("            SELECT APP_DD, APP_NO, SALE_AMT, CARD_NO ,TRANIDX, RTN_CD \r\n"); 
			strbuf.append("        FROM TB_MNG_DEPDATA \r\n");
			strbuf.append("        )T2 ON( \r\n");
			strbuf.append("            T1.APPDD=T2.APP_DD AND \r\n");
			strbuf.append("            T1.APPNO=T2.APP_NO AND \r\n");
			strbuf.append("            T1.AMOUNT=T2.SALE_AMT AND \r\n");
			strbuf.append("            T1.CARDNO=T2.CARD_NO AND \r\n");
			strbuf.append("            T1.APPGB=CASE \r\n");
			strbuf.append("                WHEN T2.RTN_CD='60' THEN 'A' \r\n");
			strbuf.append("                WHEN T2.RTN_CD='67' THEN 'C' \r\n");
			strbuf.append("            END \r\n");
			strbuf.append("            ) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD= '" + orgcd + "' \r\n");
			strbuf.append(")T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_KOCES, PUR_CD FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES OR T1.ACQ_CD=T5.PUR_CD) \r\n");
			strbuf.append("WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where ORG_CD= '" + orgcd + "' " + depcd_where + ")" + set_where_dep + set_where_depextra +"  \r\n");
			strbuf.append("order by APPDD desc, apptm desc \r\n");
			strbuf.append(") \r\n");
			strbuf.append(") \r\n");
			
			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0307T 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0307T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
		
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");			
			strbuf.append("    DEP_NM \r\n");
			strbuf.append("    ACQ_CD \r\n");
			strbuf.append("    PUR_NM TR_ACQNM\r\n");
			strbuf.append("    TERM_ID TR_TID \r\n");
			strbuf.append("    TERM_NM \r\n");
			strbuf.append("    TRIM(TO_CHAR((ACNT),'999,999,999,999,999,999')) ACNT \r\n");
			strbuf.append("    TRIM(TO_CHAR((CCNT),'999,999,999,999,999,999')) CCNT \r\n");
			strbuf.append("    TRIM(TO_CHAR((AAMT),'999,999,999,999,999,999')) AAMT \r\n");
			strbuf.append("    TRIM(TO_CHAR((CAMT),'999,999,999,999,999,999')) CAMT \r\n");
			strbuf.append("    TRIM(TO_CHAR((TOTCNT),'999,999,999,999,999,999')) TOTCNT \r\n");
			strbuf.append("    TRIM(TO_CHAR((TOTAMT),'999,999,999,999,999,999')) TOTAMT \r\n");			
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("TID \r\n");
			strbuf.append(",ACQ_CD \r\n");
			strbuf.append(",PUR_NM \r\n");
			strbuf.append(",SUM(ACNT) ACNT \r\n");
			strbuf.append(",SUM(CCNT) CCNT \r\n");
			strbuf.append(",SUM(AAMT) AAMT \r\n");
			strbuf.append(",SUM(CAMT) CAMT \r\n");
			strbuf.append(",SUM(ACNT)+SUM(CCNT) TOTCNT \r\n");
			strbuf.append(",SUM(AAMT)-SUM(CAMT) TOTAMT \r\n");
			strbuf.append(",SUM(ABC )-SUM(CBC ) BC \r\n");
			strbuf.append(",SUM(ANH )-SUM(CNH ) NH \r\n");
			strbuf.append(",SUM(AKB )-SUM(CKB ) KB \r\n");
			strbuf.append(",SUM(ASS )-SUM(CSS ) SS \r\n");
			strbuf.append(",SUM(AHN )-SUM(CHN ) HN \r\n");
			strbuf.append(",SUM(ALO )-SUM(CLO ) LO \r\n");
			strbuf.append(",SUM(AHD )-SUM(CHD ) HD \r\n");
			strbuf.append(",SUM(ASI )-SUM(CSI ) SI \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("TID \r\n");
			strbuf.append(",ACQ_CD \r\n");
			strbuf.append(",PUR_NM \r\n");
			strbuf.append(",CASE WHEN APPGB='A' THEN COUNT(1) ELSE 0 END ACNT \r\n");
			strbuf.append(",CASE WHEN APPGB='C' THEN COUNT(1) ELSE 0 END CCNT \r\n");
			strbuf.append(",CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT \r\n");
			strbuf.append(",CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0006', '026', '1106','01') THEN SUM(AMOUNT) ELSE 0 END ABC \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0030', '018', '2211','11') THEN SUM(AMOUNT) ELSE 0 END ANH \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0001', '016', '1101','02') THEN SUM(AMOUNT) ELSE 0 END AKB \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0004', '031', '1104','06') THEN SUM(AMOUNT) ELSE 0 END ASS \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0005', '008', '1105','03') THEN SUM(AMOUNT) ELSE 0 END AHN \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0003', '047', '1103','33') THEN SUM(AMOUNT) ELSE 0 END ALO \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0002', '027', '1102','08') THEN SUM(AMOUNT) ELSE 0 END AHD \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0007', '029', '1107','07') THEN SUM(AMOUNT) ELSE 0 END ASI \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0006', '026', '1106','01') THEN SUM(AMOUNT) ELSE 0 END CBC \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0030', '018', '2211','11') THEN SUM(AMOUNT) ELSE 0 END CNH \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0001', '016', '1101','02') THEN SUM(AMOUNT) ELSE 0 END CKB \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0004', '031', '1104','06') THEN SUM(AMOUNT) ELSE 0 END CSS \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0005', '008', '1105','03') THEN SUM(AMOUNT) ELSE 0 END CHN \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0003', '047', '1103','33') THEN SUM(AMOUNT) ELSE 0 END CLO \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0002', '027', '1102','08') THEN SUM(AMOUNT) ELSE 0 END CHD \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0007', '029', '1107','07') THEN SUM(AMOUNT) ELSE 0 END CSI \r\n");
			strbuf.append("FROM ( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("SEQNO, DEP_NM, TERM_NM, TID, MID, PUR_NM, TSTAT, ACQ_CD, \r\n");
			strbuf.append("TSTAT_TXT, APPDD, APPTM, TSTAT CANDATE, OAPPDD, APPNO, APPGB, \r\n");
			strbuf.append("APPGB_TXT, CARDNO, AMOUNT, HALBU, CARDTP_TXT, SIGNCHK_TXT, \r\n");
			strbuf.append("REQ_DD, AUTHCD, REG_DD, RTN_CD, RTN_TXT, \r\n");
			strbuf.append("EXP_DD, EXT_FIELD, TRANIDX, AUTHMSG \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("SEQNO, DEP_NM, TERM_NM, TID, MID, PUR_NM, TSTAT, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("WHEN APPGB='A' AND TSTAT IS NULL THEN '승인거래' \r\n");
			strbuf.append("WHEN APPGB='A' AND TSTAT=APPDD THEN '당일취소' \r\n");
			strbuf.append("WHEN APPGB='C' AND APPDD=OAPPDD THEN '당일취소' \r\n");
			strbuf.append("WHEN APPGB='C' AND APPDD<>OAPPDD THEN '전일취소' \r\n");
			strbuf.append("WHEN APPGB='A' AND APPDD<>TSTAT AND TSTAT IS NOT NULL THEN '전일취소' \r\n");
			strbuf.append("END TSTAT_TXT, \r\n");
			strbuf.append("APPDD, APPTM, TSTAT CANDATE, OAPPDD, APPNO, APPGB, ACQ_CD, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("WHEN APPGB='A' THEN '신용승인' \r\n");
			strbuf.append("WHEN APPGB='C' THEN '신용취소' \r\n");
			strbuf.append("END APPGB_TXT, \r\n");
			strbuf.append("CARDNO, AMOUNT, HALBU, \r\n");
			strbuf.append("CASE WHEN CHECK_CARD='Y' THEN '체크카드' ELSE '신용카드' END CARDTP_TXT, \r\n");
			strbuf.append("CASE WHEN SIGNCHK='1' THEN '전자서명' ELSE '무서명' END SIGNCHK_TXT, \r\n");
			strbuf.append("REQ_DD, AUTHCD, REG_DD, RTN_CD, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("WHEN RTN_CD IS NULL THEN '결과없음' \r\n");
			strbuf.append("WHEN RTN_CD IN('60', '67') THEN '정상매입' \r\n");
			strbuf.append("WHEN RTN_CD IN('61', '64') THEN '매입반송' \r\n");
			strbuf.append("END RTN_TXT, \r\n");
			strbuf.append("EXP_DD, EXT_FIELD, TRANIDX, AUTHMSG \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("SEQNO, BIZNO, TID, MID, VANGB, MDATE, SVCGB, T1.TRANIDX, T1.APPGB, ENTRYMD, \r\n");
			strbuf.append("T1.APPDD, APPTM, T1.APPNO, T1.CARDNO, HALBU, CURRENCY, T1.AMOUNT, AMT_UNIT, AMT_TIP, AMT_TAX, \r\n");
			strbuf.append("ISS_CD, ISS_NM, ACQ_CD, ACQ_NM, AUTHCD, AUTHMSG, CARD_CODE, CHECK_CARD, OVSEA_CARD, TLINEGB, \r\n");
			strbuf.append("SIGNCHK, DDCGB, EXT_FIELD, OAPPNO, OAPPDD, OAPPTM, OAPP_AMT, ADD_GB, ADD_CID, ADD_CD, \r\n");
			strbuf.append("ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, SECTION_NO, PUR_NM, DEP_NM, EXP_DD, REQ_DD, REG_DD, RSC_CD, RTN_CD, TERM_NM, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("WHEN APPGB='C' THEN '' \r\n");
			strbuf.append("WHEN APPGB='A' THEN (SELECT C1.APPDD FROM GLOB_MNG_ICVAN C1 WHERE C1.APPGB='C' AND T1.APPDD=C1.OAPPDD AND T1.APPNO=C1.APPNO AND T1.AMOUNT=C1.AMOUNT AND T1.CARDNO=C1.CARDNO) \r\n");
			strbuf.append("END TSTAT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT EXP_DD, REQ_DD, REG_DD, APP_DD, TRANIDX, RSC_CD, RTN_CD FROM TB_MNG_DEPDATA \r\n");
			strbuf.append(")T2 ON(T1.APPDD=T2.APP_DD AND T1.TRANIDX=T2.TRANIDX) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD= '" + orgcd + "')T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD= '" + orgcd + "')T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_KOCES FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES) \r\n");
			strbuf.append("WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where ORG_CD= '" + orgcd + "'" + depcd_where + ") " + set_where_dep + set_where_depextra +" \r\n");
			strbuf.append("order by APPDD desc, apptm desc \r\n");
			strbuf.append(") \r\n");
			strbuf.append(") \r\n");
			strbuf.append(") \r\n");
			strbuf.append("GROUP BY TID, APPGB, ACQ_CD, PUR_NM \r\n");
			strbuf.append(") \r\n");
			strbuf.append("GROUP BY TID, PUR_NM, ACQ_CD \r\n");
			strbuf.append(")T2 \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD= '" + orgcd + "')T3 ON(T2.TID=T3.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD= '" + orgcd + "')T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");

			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0308 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0308(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_req = resultList.get(4);
		String set_where_depextra = resultList.get(6);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    TR_DEPNM, \r\n");
			strbuf.append("    TR_TIDNM, \r\n");
			strbuf.append("    TR_TID, \r\n");
			strbuf.append("    TR_ACQNM, \r\n");
			strbuf.append("    TR_MID, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(REQ_DD   , 'YYYYMMDD'), 'YYYY-MM-DD') REQ_DD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(TR_APP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') TR_APP_DD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(EXP_DD   , 'YYYYMMDD'), 'YYYY-MM-DD') EXP_DD, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(CNT))       ,'999,999,999,999,999,999'))  CNT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(AMT))       ,'999,999,999,999,999,999'))  AMT, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(DP_FEE))    ,'999,999,999,999,999,999'))  DP_FEE, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SUM(DP_EXP_AMT)),'999,999,999,999,999,999'))  DP_EXP_AMT, \r\n");
			strbuf.append("    ACQ_CD \r\n");
			strbuf.append("FROM ( \r\n");			
			strbuf.append("SELECT \r\n");
			strbuf.append("    T4.DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    T2.TERM_NM TR_TIDNM, \r\n");
			strbuf.append("    T1.TID TR_TID, \r\n");
			strbuf.append("    T3.PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    T1.MID TR_MID, \r\n");
			strbuf.append("    T1.REQ_DD REQ_DD, \r\n");
			strbuf.append("    T1.APP_DD TR_APP_DD, \r\n");
			strbuf.append("    T1.EXP_DD EXP_DD, \r\n");
			strbuf.append("    (T1.ACNT+T1.CCNT) CNT, \r\n");
			strbuf.append("    (T1.AAMT-T1.CAMT) AMT, \r\n");
			strbuf.append("    (T1.AFEE-T1.CFEE) DP_FEE, \r\n");
			strbuf.append("    T1.DP_EXP_AMT DP_EXP_AMT, \r\n");
			strbuf.append("    T1.ACQ_CD \r\n");
			strbuf.append("FROM ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        DEP_CD, \r\n");
			strbuf.append("        TID, \r\n");
			strbuf.append("        MID, \r\n");
			strbuf.append("        ACQ_CD, \r\n");
			strbuf.append("        REQ_DD, \r\n");
			strbuf.append("        APP_DD, \r\n");
			strbuf.append("        EXP_DD, \r\n");
			strbuf.append("        SUM(ACNT) ACNT, \r\n");
			strbuf.append("        SUM(CCNT) CCNT, \r\n");
			strbuf.append("        SUM(AAMT) AAMT, \r\n");
			strbuf.append("        SUM(CAMT) CAMT, \r\n");
			strbuf.append("        SUM(AFEE) AFEE, \r\n");
			strbuf.append("        SUM(CFEE) CFEE, \r\n");
			strbuf.append("        ((AAMT-CAMT)-(AFEE-CFEE)) DP_EXP_AMT \r\n");
			strbuf.append("    FROM ( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            DEP_CD, \r\n");
			strbuf.append("            TID, \r\n");
			strbuf.append("            MID, \r\n");
			strbuf.append("            ACQ_CD, \r\n");
			strbuf.append("            REQ_DD, \r\n");
			strbuf.append("            APP_DD, \r\n");
			strbuf.append("            EXP_DD, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' THEN COUNT(*) ELSE 0 END ACNT, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' THEN COUNT(*) ELSE 0 END CCNT, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END AAMT, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END CAMT, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' THEN SUM(FEE) ELSE 0 END AFEE, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' THEN SUM(FEE) ELSE 0 END CFEE \r\n");
			strbuf.append("        FROM ( \r\n");
			strbuf.append("            SELECT \r\n");
			strbuf.append("                DEP_CD, \r\n");
			strbuf.append("                TID, \r\n");
			strbuf.append("                ACQ_CD, \r\n");
			strbuf.append("                MID, \r\n");
			strbuf.append("                REQ_DD, \r\n");
			strbuf.append("                APP_DD, \r\n");
			strbuf.append("                EXP_DD, \r\n");
			strbuf.append("                RTN_CD, \r\n");
			strbuf.append("                FEE, \r\n");
			strbuf.append("                SALE_AMT \r\n");
			strbuf.append("            FROM TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("            WHERE (RSC_CD='00' OR RSC_CD='0000') " + set_where_dep + set_where_depextra + "\r\n");
			strbuf.append("        ) \r\n");
			strbuf.append("        GROUP BY TID, MID, DEP_CD, RTN_CD, ACQ_CD, APP_DD, REQ_DD, EXP_DD \r\n");
			strbuf.append("    ) \r\n");
			strbuf.append("    GROUP BY TID, DEP_CD, APP_DD, REQ_DD, MID, ACQ_CD, EXP_DD, AAMT, CAMT, AFEE, CFEE \r\n");
			strbuf.append(") T1 \r\n");
			strbuf.append("INNER JOIN ( \r\n");
			strbuf.append("SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST \r\n");
			strbuf.append(") T2 ON (T1.TID=T2.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN ( \r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD = '" + orgcd + "' \r\n");
			strbuf.append(") T4 ON (T1.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN ( \r\n");
			strbuf.append("SELECT PUR_NM, PUR_OCD FROM TB_BAS_PURINFO \r\n");
			strbuf.append(") T3 ON (T1.ACQ_CD= T3.PUR_OCD) \r\n");
			strbuf.append(") GROUP BY TR_DEPNM, TR_TIDNM, TR_TID, TR_ACQNM, TR_MID, REQ_DD, TR_APP_DD, EXP_DD, ACQ_CD \r\n");		
			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * get_sub0308T 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0308T(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_req = resultList.get(4);
		String set_where_depextra = resultList.get(6);
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n");
			strbuf.append("    DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    TID, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(APP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') APP_DD, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(REQ_DD, 'YYYYMMDD'), 'YYYY-MM-DD') REQ_DD, \r\n");
			strbuf.append("    TRIM(TO_CHAR(((KB+NH+LO+BC+SS+SI+HN+HD)),'999,999,999,999,999,999'))  TOTAM, \r\n");
			strbuf.append("    TRIM(TO_CHAR((KB),'999,999,999,999,999,999')) KB, \r\n");
			strbuf.append("    TRIM(TO_CHAR((NH),'999,999,999,999,999,999')) NH, \r\n");
			strbuf.append("    TRIM(TO_CHAR((LO),'999,999,999,999,999,999')) LO, \r\n");
			strbuf.append("    TRIM(TO_CHAR((BC),'999,999,999,999,999,999')) BC, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SS),'999,999,999,999,999,999')) SS, \r\n");
			strbuf.append("    TRIM(TO_CHAR((SI),'999,999,999,999,999,999')) SI, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HN),'999,999,999,999,999,999')) HN, \r\n");
			strbuf.append("    TRIM(TO_CHAR((HD),'999,999,999,999,999,999')) HD \r\n");
			strbuf.append("FROM ( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("        TID, \r\n");
			strbuf.append("        DEP_CD, \r\n");
			strbuf.append("        APP_DD, \r\n");
			strbuf.append("        REQ_DD, \r\n");
			strbuf.append("        (SUM(KBA)-SUM(KBC)) KB, \r\n");
			strbuf.append("        (SUM(NHA)-SUM(NHC)) NH, \r\n");
			strbuf.append("        (SUM(LOA)-SUM(LOC)) LO, \r\n");
			strbuf.append("        (SUM(BCA)-SUM(BCC)) BC, \r\n");
			strbuf.append("        (SUM(SSA)-SUM(SSC)) SS, \r\n");
			strbuf.append("        (SUM(SIA)-SUM(SIC)) SI, \r\n");
			strbuf.append("        (SUM(HNA)-SUM(HNC)) HN, \r\n");
			strbuf.append("        (SUM(HDA)-SUM(HDC)) HD \r\n");
			strbuf.append("    FROM ( \r\n");
			strbuf.append("        SELECT \r\n");
			strbuf.append("            TID, \r\n");
			strbuf.append("            APP_DD, \r\n");
			strbuf.append("            REQ_DD, \r\n");
			strbuf.append("            DEP_CD, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' AND ACQ_CD IN ('VC0001', '016', '1101', '02') THEN SUM(SALE_AMT) ELSE 0 END KBA, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' AND ACQ_CD IN ('VC0001', '016', '1101', '02') THEN SUM(SALE_AMT) ELSE 0 END KBC, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' AND ACQ_CD IN ('VC0030', '018', '2211', '11') THEN SUM(SALE_AMT) ELSE 0 END NHA, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' AND ACQ_CD IN ('VC0030', '018', '2211', '11') THEN SUM(SALE_AMT) ELSE 0 END NHC, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' AND ACQ_CD IN ('VC0003', '047', '1103', '33') THEN SUM(SALE_AMT) ELSE 0 END LOA, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' AND ACQ_CD IN ('VC0003', '047', '1103', '33') THEN SUM(SALE_AMT) ELSE 0 END LOC, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' AND ACQ_CD IN ('VC0006', '026', '1106', '01') THEN SUM(SALE_AMT) ELSE 0 END BCA, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' AND ACQ_CD IN ('VC0006', '026', '1106', '01') THEN SUM(SALE_AMT) ELSE 0 END BCC, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' AND ACQ_CD IN ('VC0004', '031', '1104', '06') THEN SUM(SALE_AMT) ELSE 0 END SSA, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' AND ACQ_CD IN ('VC0004', '031', '1104', '06') THEN SUM(SALE_AMT) ELSE 0 END SSC, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' AND ACQ_CD IN ('VC0007', '029', '1107', '07') THEN SUM(SALE_AMT) ELSE 0 END SIA, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' AND ACQ_CD IN ('VC0007', '029', '1107', '07') THEN SUM(SALE_AMT) ELSE 0 END SIC, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' AND ACQ_CD IN ('VC0005', '008', '1105', '03') THEN SUM(SALE_AMT) ELSE 0 END HNA, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' AND ACQ_CD IN ('VC0005', '008', '1105', '03') THEN SUM(SALE_AMT) ELSE 0 END HNC, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='60' AND ACQ_CD IN ('VC0002', '027', '1102', '08') THEN SUM(SALE_AMT) ELSE 0 END HDA, \r\n");
			strbuf.append("            CASE WHEN RTN_CD='67' AND ACQ_CD IN ('VC0002', '027', '1102', '08') THEN SUM(SALE_AMT) ELSE 0 END HDC \r\n");
			strbuf.append("        FROM ( \r\n");
			strbuf.append("            SELECT \r\n");
			strbuf.append("                TID, DEP_CD, RTN_CD, ACQ_CD, SALE_AMT, APP_DD, REQ_DD \r\n");
			strbuf.append("            FROM \r\n");
			strbuf.append("                TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("            WHERE (RSC_CD='00' OR RSC_CD='0000')" + set_where_dep + set_where_depextra +" \r\n");
			strbuf.append("        ) \r\n");
			strbuf.append("        GROUP BY TID, DEP_CD, RTN_CD, ACQ_CD, APP_DD, REQ_DD \r\n");
			strbuf.append("    ) GROUP BY TID, DEP_CD, APP_DD, REQ_DD \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST \r\n");
			strbuf.append(")T2 ON(T1.TID=T2.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD = '" + orgcd + "')T4 ON(T1.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("WHERE TERM_NM IS NOT NULL  \r\n");

			
			//System.lineSeparator()
			
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * get_sub0309 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_sub0309(JSONArray jary,String DEBUG,HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("SELECT \r\n");
			strbuf.append("    T2.DEP_NM TR_DEPNM, \r\n");
			strbuf.append("    T3.PUR_NM TR_ACQNM, \r\n");
			strbuf.append("    MID TR_MID, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(EXP_DD, 'YYYYMMDD'), 'YYYY-MM-DD') EXP_DD, \r\n");
			strbuf.append("    TRIM(TO_CHAR((EXP_AMT),'999,999,999,999,999,999')) EXP_AMT, \r\n");
			strbuf.append("    ACC_TXT BANK_MSG, \r\n");
			strbuf.append("    TO_CHAR(TO_DATE(UPDATE_DD, 'YYYYMMDD'), 'YYYY-MM-DD')  UPLOAD_DD \r\n");
			strbuf.append("FROM TB_MNG_BANKDATA T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD = '" + orgcd + "'" +  depcd_where +")T2 ON(T1.DEP_CD=T2.DEP_CD) \r\n");
			strbuf.append("left outer join(select pur_nm, pur_ocd from tb_bas_purinfo)T3 ON (T1.ACQ_CD= T3.pur_ocd) \r\n");
			strbuf.append("WHERE MID IS NOT NULL " + set_where_dep  + set_where_depextra +" \r\n");
			//DEPNM
			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			int orn = 1;
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {				
					
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					if(!Objects.equals(id, "ORN") && !Objects.equals(id, null)) {
					System.out.println(id);
					
					if(Objects.equals(rs.getString(id), null)) {
						jsonob.put(id,"");	
					}else{
						jsonob.put(id,rs.getString(id));	
					}
					System.out.println("success");
					}else {
					jsonob.put("ORN",orn);
					orn++;
					}
					}
	            
				jsonary.add(jsonob);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * insert_bankdata 
	 * @param jary(tb_sys_domain)
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-22 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray insert_bankdata(String DEBUG,HashMap<String, String> whereqry,
			String exp_dd, String acc_txt, String mid, int exp_amt, String update_dd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
		util_manager util = new util_manager();
		List<String> resultList = util.make_query(whereqry);
		
		String orgcd = resultList.get(0);
		String depcd_where = resultList.get(1);
		String set_where = resultList.get(2);
		String set_where_dep = resultList.get(3);
		String set_where_depextra = resultList.get(6);
		
		try {
			strbuf = new StringBuffer();
			//�뜝�룞�삕�뜝�룞�삕�뜝�뙃琉꾩삕
			strbuf.append("INSERT INTO TB_MNG_BANKDATA \r\n");
			strbuf.append("(ORG_CD, DEP_CD, ACC_TXT, MID, EXP_DD, EXP_AMT, UPDATE_DD) \r\n");
			strbuf.append("VALUES('"+ orgcd + "','" + depcd_where + "','" + acc_txt + "','" + mid + "','" + " \r\n");
			strbuf.append(exp_dd+"','" + exp_amt + "','" + update_dd + "') \r\n");
		
			//DEPNM
			
			//System.lineSeparator()
			/**
			 * ----------�뜝�룞�삕�뜝�룞�삕�뜝占�---------------
			 */
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			if(Objects.equals(DEBUG,"Y")) {
				JSONObject debugqry = new JSONObject();
				String qry = "<br><br>" + strbuf.toString().replace("\r\n", "<br>").replace("\t","") + "<br>";
				debugqry.put("qry", qry);
				jsonary.add(debugqry);
				return jsonary;
			}

			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			
			rs = stmt.executeQuery();

			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	
	
	
	
	
	/**
	 * depcd �뜝�떆琉꾩삕 Select Query
	 * @param orgcd
	 * @return json�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 dhx�뜝�룞�삕�뜝�떇�슱�삕 �뜝�듅怨ㅼ삕
	 * 2023-02-01 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_depcd(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT DEP_CD,DEP_NM FROM TB_BAS_DEPART WHERE ORG_CD = '" + orgcd + "'");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("dep_cd",rs.getString("DEP_CD"));
	            jsonob.put("dep_nm",rs.getString("DEP_NM"));
				jsonary.add(jsonob);

				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * acqpcd �뜝�떆琉꾩삕 Select Query
	 * @return json�뜝�룞�삕�뜝�룞�삕
	 * 2023-02-13 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_acqcd() {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT PUR_CD,PUR_NM,PUR_KOCES FROM TB_BAS_PURINFO WHERE PUR_USE='Y' ");
			strbuf.append("ORDER BY PUR_NM ASC");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("pur_cd",rs.getString("PUR_CD"));
	            jsonob.put("pur_nm",rs.getString("PUR_NM"));
	            jsonob.put("pur_koces",rs.getString("PUR_KOCES"));
	            
				jsonary.add(jsonob);

				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * tidcd �뜝�떆琉꾩삕 Select Query
	 * @param orgcd
	 * @return json
	 * 2023-02-13 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_tidcd(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD = '" + orgcd +"'");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("tid_cd",rs.getString("TERM_ID"));
	            jsonob.put("tid_nm",rs.getString("TERM_NM"));
				jsonary.add(jsonob);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * midcd �뜝�떆琉꾩삕 Select Query
	 * @param orgcd
	 * @return json
	 * 2023-02-13 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 */
	public JSONArray get_midcd(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT MID FROM TB_BAS_MIDMAP WHERE ORG_CD = '" + orgcd + "'");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("MID",rs.getString("MID"));
				jsonary.add(jsonob);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	
	/**
	 * �뜝�뙣�뙋�삕 �뜝�떆琉꾩삕 Select Query
	 * @param auth_seq,orgcd
	 * @return json
	 * 2023-02-13 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_menu(String auth_seq, String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT ");
			strbuf.append(" A.PROGRAM_SEQ AS PROGRAM_SEQ, ");
			strbuf.append(" B.PROGRAM_NAME MENU_NAME, ");
			strbuf.append(" B.DEPTH MENU_DEPTH, ");
			strbuf.append(" B.PARENT_SEQ PARENT_SEQ, ");
			strbuf.append(" A.ENABLE_READ AUTH_R, ");
			strbuf.append(" A.ENABLE_CREATE AUTH_C, ");
			strbuf.append(" A.ENABLE_UPDATE AUTH_U, ");
			strbuf.append(" A.ENABLE_DELETE AUTH_D, ");
			strbuf.append(" B.SRC_LOCATION MURL ");
			strbuf.append(" FROM TB_SYS_MENU A ");
			strbuf.append(" LEFT OUTER JOIN ");
			strbuf.append(" (SELECT PROGRAM_SEQ, PROGRAM_NAME, PARENT_SEQ, DEPTH, SRC_LOCATION, SORT FROM TB_SYS_PROGRAM) B ");
			strbuf.append(" ON (A.PROGRAM_SEQ=B.PROGRAM_SEQ) ");
			strbuf.append(" WHERE A.AUTH_SEQ = '" + auth_seq +"' AND ORGCD = '" + orgcd +"'");
			strbuf.append(" ORDER BY B.SORT ASC ");
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				jsonob.put("MENU_SEQ",rs.getString("PROGRAM_SEQ"));
	            jsonob.put("MENU_NAME",rs.getString("MENU_NAME"));
	            jsonob.put("MENU_DEPTH",rs.getString("MENU_DEPTH"));
	            jsonob.put("PARENT_SEQ",rs.getString("PARENT_SEQ"));
	            jsonob.put("AUTH_R",rs.getString("AUTH_R"));
	            jsonob.put("AUTH_C",rs.getString("AUTH_C"));
	            jsonob.put("AUTH_U",rs.getString("AUTH_U"));
	            jsonob.put("AUTH_D",rs.getString("AUTH_D"));
	            jsonob.put("MURL",rs.getString("MURL"));
				jsonary.add(jsonob);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * �뜝�룞�삕�뜝�떆節륁삕�뜝占� �뜝�뙣�뙋�삕 �뜝�떆琉꾩삕 Select Query
	 * @param user_id
	 * @return json
	 * 2023-02-13 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_favorite(String userid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT T1.PROGRAM_SEQ,T2.PROGRAM_NAME,T2.SRC_LOCATION,T1.SORT FROM TB_SYS_FAVORITE T1 ");
			strbuf.append("INNER JOIN TB_SYS_PROGRAM T2 ");
			strbuf.append("ON (T1.PROGRAM_SEQ=T2.PROGRAM_SEQ) ");
			strbuf.append("WHERE T1.USER_ID = '" + userid +"' ORDER BY SORT ASC");
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
				
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				jsonob.put("PROGRAM_SEQ",rs.getString("PROGRAM_SEQ"));
				jsonob.put("PROGRAM_NAME",rs.getString("PROGRAM_NAME"));
	            jsonob.put("SRC_LOCATION",rs.getString("SRC_LOCATION"));
	            jsonob.put("SORT",rs.getString("SORT"));
				jsonary.add(jsonob);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * �뜝�룞�삕�뜝�떆節륁삕�뜝占� �뜝�뙣�뙋�삕 �뜝�떆琉꾩삕 INSERT Query
	 * @param user_id,program_seq,sort
	 * @return 1 : �뜝�룞�삕�뜝�룞�삕 / 0 : �뜝�룞�삕�뜝�룞�삕 (�뜝�떥�눦�삕�듃�뜝�룞�삕 �뜝�떥�슱�삕 �뜝�룞�삕)
	 * 2023-02-13 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public int insert_favorite(String userid,String program_seq,String sort) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("INSERT INTO TB_SYS_FAVORITE (USER_ID,PROGRAM_SEQ,SORT,USE_YN) VALUES('" + userid + "','" + program_seq + "','" + sort + "','Y')");
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			int rowsInserted = stmt.executeUpdate();
			
            if (rowsInserted > 0) {
                return rowsInserted;
            } else {
                return 0;
            }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return 0;
	}
	
	/**
	 * �뜝�룞�삕�뜝�떆節륁삕�뜝占� �뜝�뙣�뙋�삕 �뜝�떆琉꾩삕 DELETE Query
	 * @param user_id,program_seq,sort
	 * @return 1 : �뜝�룞�삕�뜝�룞�삕 / 0 : �뜝�룞�삕�뜝�룞�삕 (�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 �뜝�떥�슱�삕 �뜝�룞�삕)
	 * 2023-02-13 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public int del_favorite(String userid,String program_seq,String sort) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("DELETE FROM TB_SYS_FAVORITE WHERE ");
			strbuf.append("USER_ID = '" + userid + "' AND ");
			strbuf.append("PROGRAM_SEQ = '" + program_seq + "' AND ");
			strbuf.append("SORT = '" + sort + "'");			
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕

			
			int rowsInserted = stmt.executeUpdate();
			
            if (rowsInserted > 0) {
                return rowsInserted;
            } else {
                return 0;
            }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return 0;
	}
	
	/**
	 * �뜝�룞�삕�뜝�떆節륁삕�뜝占� �뜝�뙣�뙋�삕 �뜝�떆琉꾩삕 UPDATE Query
	 * @param user_id,program_seq,sort
	 * @return 1 : �뜝�룞�삕�뜝�룞�삕 / 0 : �뜝�룞�삕�뜝�룞�삕 (�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕 �뜝�떥�슱�삕 �뜝�룞�삕)
	 * 2023-02-13 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public int mod_favorite(String userid,String program_seq,String sort,String new_sort) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("UPDATE TB_SYS_FAVORITE SET SORT = '" + new_sort + "' WHERE ");
			strbuf.append("USER_ID = '" + userid + "' AND ");
			strbuf.append("PROGRAM_SEQ = '" + program_seq + "' AND ");
			strbuf.append("SORT = '" + sort + "'");		
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
		
			int rowsInserted = stmt.executeUpdate();
			
            if (rowsInserted > 0) {
                return rowsInserted;
            } else {
                return 0;
            }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return 0;
	}
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕�솕�뜝�룞�삕 �뜝�룞�삕�뜝�뙓釉앹삕�뜝�룞�삕 �뜝�떛�븘�슱�삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕
	 * @param orgcd, APPDD (ex : 202302)
	 * @return SVCGB(�뜝�떊�슱�삕,�뜝�룞�삕�뜝�룞�삕,�뜝�룞�삕�뜝�룞�삕IC) , APPDD(20230222), AMT(�뜝�뙠�뼲�삕)
	 * 2023-03-08 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONObject get_main_amt(String orgcd, String APPDD) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		JSONObject jsonob = new JSONObject();
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n ");
			strbuf.append("SVCGB,APPDD, TRIM(TO_CHAR((AAMT-CAMT),'999,999,999,999,999,999')) AMT\r\n ");
			strbuf.append("FROM(\r\n ");
			strbuf.append("SELECT\r\n ");
			strbuf.append("CASE\r\n ");
			strbuf.append("WHEN SVCGB ='CC' THEN '신용'\r\n ");
			strbuf.append("WHEN SVCGB ='CB' THEN '현금'\r\n ");
			strbuf.append("WHEN SVCGB ='IC' THEN '현금IC'\r\n ");
			strbuf.append("END SVCGB\r\n ");
			strbuf.append(",APPDD,SUM(AAMT) AAMT,SUM(CAMT) CAMT\r\n ");
			strbuf.append("FROM(\r\n ");
			strbuf.append("SELECT\r\n ");
			strbuf.append("SVCGB,\r\n ");
			strbuf.append("APPDD,\r\n ");
			strbuf.append("CASE\r\n ");
			strbuf.append("WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0\r\n ");
			strbuf.append("END AAMT,\r\n ");
			strbuf.append("CASE\r\n ");
			strbuf.append("WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0\r\n ");
			strbuf.append("END CAMT\r\n ");
			strbuf.append("FROM \r\n ");
			strbuf.append("GLOB_MNG_ICVAN\r\n ");
			strbuf.append("WHERE AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where ORG_CD = '" + orgcd + "' )  AND SUBSTR(APPDD,0,6)='"+APPDD+"'\r\n ");
			strbuf.append("GROUP BY APPDD, APPGB,SVCGB\r\n ");
			strbuf.append(")\r\n ");
			strbuf.append("GROUP BY APPDD,SVCGB\r\n ");
			strbuf.append(")\r\n ");

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
	            jsonob.put(rs.getString("SVCGB")+rs.getString("APPDD"),rs.getString("AMT").trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonob;
	}

	/**
	 * �뜝�룞�삕�뜝�룞�삕�솕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕 �뜝�뙆怨ㅼ삕�셿�뜝占�, �뜝�떎�눦�삕 �뜝�룞�삕�쉶
	 * @param orgcd, APPDD (ex : 202302) **6�뜝�뙓紐뚯삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕�뜝�떙�궪�삕 8�뜝�뙓紐뚯삕�뜝�룞�삕 �뜝�떦釉앹삕 �뜝�떙�궪�삕
	 * @return 
	 * 2023-03-08 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONObject get_main_month_amt(String orgcd, String where_qry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		JSONObject jsonob = new JSONObject();
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT\r\n");
			strbuf.append("TO_CHAR(NVL(SUM(CCA-CCC),0),'999,999,999,999,999') AS CC_AMT\r\n");
			strbuf.append(",TO_CHAR(NVL(SUM(CBA-CBC),0),'999,999,999,999,999') AS CB_AMT\r\n");
			strbuf.append(",TO_CHAR(NVL(SUM(ICA-ICC),0),'999,999,999,999,999') AS IC_AMT\r\n");
			strbuf.append(",TO_CHAR(NVL(SUM(CCA+CBA+ICA-CCC-CBC-ICC),0),'999,999,999,999,999') AS SUM_AMT\r\n");
			strbuf.append(",TO_CHAR(SUM(CCCNT),'999,999,999,999,999') AS CC_CNT\r\n");
			strbuf.append(",TO_CHAR(SUM(CBCNT),'999,999,999,999,999') AS CB_CNT\r\n");
			strbuf.append(",TO_CHAR(SUM(ICCNT),'999,999,999,999,999') AS IC_CNT\r\n");
			strbuf.append(",TO_CHAR(NVL(SUM(CCCNT+CBCNT+ICCNT),0),'999,999,999,999,999') AS SUM_CNT\r\n");
			strbuf.append("FROM\r\n");
			strbuf.append("(\r\n");
			strbuf.append("SELECT\r\n");
			strbuf.append("SUM(CASE WHEN SVCGB = 'CC' AND APPGB = 'A' THEN AMOUNT ELSE 0 END) AS CCA\r\n");
			strbuf.append(",SUM(CASE WHEN SVCGB = 'CC' AND APPGB = 'C' THEN AMOUNT ELSE 0 END) AS CCC\r\n");
			strbuf.append(",SUM(CASE WHEN SVCGB = 'CB' AND APPGB = 'A' THEN AMOUNT ELSE 0 END) AS CBA\r\n");
			strbuf.append(",SUM(CASE WHEN SVCGB = 'CB' AND APPGB = 'C' THEN AMOUNT ELSE 0 END) AS CBC\r\n");
			strbuf.append(",SUM(CASE WHEN SVCGB = 'IC' AND APPGB = 'A' THEN AMOUNT ELSE 0 END) AS ICA\r\n");
			strbuf.append(",SUM(CASE WHEN SVCGB = 'IC' AND APPGB = 'C' THEN AMOUNT ELSE 0 END) AS ICC\r\n");
			strbuf.append(",COUNT(CASE WHEN SVCGB = 'CC' THEN 1 END) AS CCCNT\r\n");
			strbuf.append(",COUNT(CASE WHEN SVCGB = 'CB' THEN 1 END) AS CBCNT\r\n");
			strbuf.append(",COUNT(CASE WHEN SVCGB = 'IC' THEN 1 END) AS ICCNT\r\n");
			strbuf.append("FROM GLOB_MNG_ICVAN\r\n");
			strbuf.append("WHERE TID IN (SELECT TID FROM TB_BAS_TIDMAP WHERE ORG_CD = '" + orgcd + "')\r\n");
			strbuf.append(where_qry);
			strbuf.append(")\r\n");


			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
	            jsonob.put("CC_AMT",rs.getString("CC_AMT").trim());
	            jsonob.put("CB_AMT",rs.getString("CB_AMT").trim());
	            jsonob.put("IC_AMT",rs.getString("IC_AMT").trim());
	            jsonob.put("SUM_AMT",rs.getString("SUM_AMT").trim());
	            jsonob.put("CC_CNT",rs.getString("CC_CNT").trim());
	            jsonob.put("CB_CNT",rs.getString("CB_CNT").trim());
	            jsonob.put("IC_CNT",rs.getString("IC_CNT").trim());
	            jsonob.put("SUM_CNT",rs.getString("SUM_CNT").trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonob;
	}
	

	/**
	 * �뜝�룞�삕�뜝�룞�삕�솕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕 �뜝�뙆怨ㅼ삕�셿�뜝占�, �뜝�떎�눦�삕 �뜝�룞�삕�쉶
	 * @param orgcd, APPDD (ex : 20230222)
	 * @return 
	 * 2023-03-08 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONObject get_main_daliy_depdata(String orgcd, String expdd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		JSONObject jsonob = new JSONObject();
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n");
			strbuf.append("TO_CHAR(NVL(SUM(SALE_AMT),0),'999,999,999,999,999') AS SALE_AMT \r\n");
			strbuf.append(",TO_CHAR(NVL(SUM(FEE),0),'999,999,999,999,999') AS FEE\r\n");
			strbuf.append(",TO_CHAR(NVL(SUM(SALE_AMT-FEE),0),'999,999,999,999,999') AS SUM\r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_MNG_DEPDATA\r\n");
			strbuf.append("WHERE\r\n");
			strbuf.append("TID IN (SELECT TID FROM TB_BAS_TIDMAP WHERE ORG_CD = '" + orgcd + "')\r\n");
			strbuf.append("AND EXP_DD = '" + expdd + "' \r\n");

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
	            jsonob.put("SALE_AMT",rs.getString("SALE_AMT").trim());
	            jsonob.put("FEE",rs.getString("FEE").trim());
	            jsonob.put("SUM",rs.getString("SUM").trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonob;
	}
	
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 * @param orgcd
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONObject get_sub0601(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONObject jsonob = new JSONObject();
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT ORG_CD,ORG_NM,ORG_NO,ORG_CORP_NO,ORG_CEO_NM,ORG_ADDR,ORG_TEL1,ORG_MEMO,ORG_EMAIL FROM TB_BAS_ORG WHERE ORG_CD = '"+orgcd+"'");

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				jsonob.put("ORG_CD",um.getString(rs.getString("ORG_CD")));
				jsonob.put("ORG_NM",um.getString(rs.getString("ORG_NM")));
	            jsonob.put("ORG_NO",um.getString(rs.getString("ORG_NO")));
	            jsonob.put("ORG_CORP_NO",um.getString(rs.getString("ORG_CORP_NO")));
	            jsonob.put("ORG_CEO_NM",um.getString(rs.getString("ORG_CEO_NM")));
	            jsonob.put("ORG_ADDR",um.getString(rs.getString("ORG_ADDR")));
	            jsonob.put("ORG_TEL1",um.getString(rs.getString("ORG_TEL1")));
	            jsonob.put("ORG_MEMO",um.getString(rs.getString("ORG_MEMO")));
	            jsonob.put("ORG_EMAIL",um.getString(rs.getString("ORG_EMAIL")));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonob;
		
		
	}
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕_�뜝�룞�삕�뜝�룞�삕
	 * @param orgcd
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0601_user(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray jsonary = new JSONArray();

		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n");
			strbuf.append("MEM_CD\r\n");
			strbuf.append(", ORG_CD\r\n");
			strbuf.append(", T1.DEP_CD\r\n");
			strbuf.append(", DEP_NM\r\n");
			strbuf.append(", AUTH_SEQ\r\n");
			strbuf.append(", USER_ID\r\n");
			strbuf.append(", USER_PW\r\n");
			strbuf.append(", USER_NM\r\n");
			strbuf.append(", USER_TEL1\r\n");
			strbuf.append(", USER_TEL2\r\n");
			strbuf.append(", USER_MEMO\r\n");
			strbuf.append(", USER_EMAIL\r\n");
			strbuf.append(", USER_FAX\r\n");
			strbuf.append(", USER_LV\r\n");
			strbuf.append(", TO_CHAR(INS_DT,'YYYY-MM-DD HH24:MI:SS') INS_DT\r\n");
			strbuf.append("FROM\r\n");
			strbuf.append("TB_BAS_USER T1\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART\r\n");
			strbuf.append(")T2 ON(T1.DEP_CD=T2.DEP_CD)\r\n");
			strbuf.append("WHERE \r\n");
			strbuf.append("ORG_CD='"+orgcd+"'\r\n");
			strbuf.append("ORDER BY USER_ID ASC, INS_DT ASC\r\n");


			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("MEM_CD",um.getString(rs.getString("MEM_CD")));
	            jsonob.put("ORG_CD",um.getString(rs.getString("ORG_CD")));
	            jsonob.put("DEP_CD",um.getString(rs.getString("DEP_CD")));
	            jsonob.put("DEP_NM",um.getString(rs.getString("DEP_NM")));
	            jsonob.put("AUTH_SEQ",um.getString(rs.getString("AUTH_SEQ")));
	            jsonob.put("USER_ID",um.getString(rs.getString("USER_ID")));
	            jsonob.put("USER_NM",um.getString(rs.getString("USER_NM")));
	            jsonob.put("USER_TEL1",um.getString(rs.getString("USER_TEL1")));
	            jsonob.put("USER_TEL2",um.getString(rs.getString("USER_TEL2")));
	            jsonob.put("USER_EMAIL",um.getString(rs.getString("USER_EMAIL")));
	            jsonob.put("INS_DT",um.getString(rs.getString("INS_DT")));
	            jsonob.put("USER_LV",um.getString(rs.getString("USER_LV")));
	            jsonob.put("INS_ID",um.getString(rs.getString("USER_FAX")));
	            jsonary.add(jsonob);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
		
		
	}
	
	/**
	 * 
	 * @param orgcd
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0603(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray jsonary = new JSONArray();

		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n");
			strbuf.append("ORG_CD\r\n");
			strbuf.append(", DEP_CD\r\n");
			strbuf.append(", DEP_NM\r\n");
			strbuf.append(", DEP_ADM_USER\r\n");
			strbuf.append(", DEP_ADDR\r\n");
			strbuf.append(", DEP_TEL1\r\n");
			strbuf.append(", DEP_EMAIL\r\n");
			strbuf.append(", DEP_SORT\r\n");
			strbuf.append(", DEP_TYPE\r\n");
			strbuf.append(", TO_CHAR(DEP_INDT,'YYYY-MM-DD HH24:MI:SS') DEP_INDT\r\n");
			strbuf.append("FROM\r\n");
			strbuf.append("TB_BAS_DEPART\r\n");
			strbuf.append("WHERE \r\n");
			strbuf.append("ORG_CD='"+orgcd+"'\r\n");
			strbuf.append("ORDER BY DEP_INDT ASC\r\n");



			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("DEP_CD",um.getString(rs.getString("DEP_CD")));
	            jsonob.put("DEP_NM",um.getString(rs.getString("DEP_NM")));
	            jsonob.put("DEP_ADM_USER",um.getString(rs.getString("DEP_ADM_USER")));
	            jsonob.put("DEP_ADDR",um.getString(rs.getString("DEP_ADDR")));
	            jsonob.put("DEP_TEL1",um.getString(rs.getString("DEP_TEL1")));
	            jsonob.put("DEP_EMAIL",um.getString(rs.getString("DEP_EMAIL")));
	            jsonob.put("DEP_TYPE",um.getString(rs.getString("DEP_TYPE")));
	            jsonob.put("DEP_INDT",um.getString(rs.getString("DEP_INDT")));
	            jsonary.add(jsonob);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�샇 �뜝�룞�삕�뜝�룞�삕
	 * @param orgcd,depcd
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0604_01(String orgcd,String depcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray jsonary = new JSONArray();

		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n");
			strbuf.append("MER_CD\r\n");
			strbuf.append(", ORG_CD\r\n");
			strbuf.append(", T3.DEP_CD\r\n");
			strbuf.append(", DEP_NM\r\n");
			strbuf.append(", T1.PUR_CD\r\n");
			strbuf.append(", PUR_NM\r\n");
			strbuf.append(", MER_NO\r\n");
			strbuf.append(", MER_ST\r\n");
			strbuf.append(", MER_ET\r\n");
			strbuf.append(", FEE01\r\n");
			strbuf.append(", FEE02\r\n");
			strbuf.append(", FEE03\r\n");
			strbuf.append(", VAN\r\n");
			strbuf.append(", TO_CHAR(INT_DT,'YYYY-MM-DD HH24:MI:SS') INS_DT\r\n");
			strbuf.append("FROM\r\n");
			strbuf.append("TB_BAS_MERINFO T1\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT PUR_NM, PUR_CD FROM TB_BAS_PURINFO\r\n");
			strbuf.append(")T2 ON(T1.PUR_CD=T2.PUR_CD)\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART\r\n");
			strbuf.append(")T3 ON(T1.DEP_CD=T3.DEP_CD)\r\n");
			strbuf.append("WHERE \r\n");
			strbuf.append("T1.ORG_CD='"+orgcd+"' AND T1.DEP_CD='"+depcd+"'\r\n");




			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("DEP_NM",um.getString(rs.getString("DEP_NM")));
	            jsonob.put("PUR_CD",um.getString(rs.getString("PUR_CD")));
	            jsonob.put("PUR_NM",um.getString(rs.getString("PUR_NM")));
	            jsonob.put("MER_NO",um.getString(rs.getString("MER_NO")));
	            jsonob.put("VAN",um.getString(rs.getString("VAN")));
	            jsonob.put("FEE01",um.getString(rs.getString("FEE01")));
	            jsonob.put("FEE02",um.getString(rs.getString("FEE02")));
	            jsonob.put("FEE03",um.getString(rs.getString("FEE03")));
	            jsonob.put("INS_DT",um.getString(rs.getString("INS_DT")));
	            jsonary.add(jsonob);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�샇 �뜝�룞�삕�뜝�룞�삕�뜝�떕占�
	 * @param orgcd
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0604_02(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray jsonary = new JSONArray();

		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n");
			strbuf.append("ORG_CD\r\n");
			strbuf.append(",T1.DEP_CD\r\n");
			strbuf.append(",T3.DEP_NM\r\n");
			strbuf.append(",T2.PUR_NM\r\n");
			strbuf.append(",T5.VAN\r\n");
			strbuf.append(",MID\r\n");
			strbuf.append(",TO_CHAR(INSTIME,'YYYY-MM-DD HH24:MI:SS') INS_DT\r\n");
			strbuf.append(",INSUSER\r\n");
			strbuf.append("FROM\r\n");
			strbuf.append("TB_BAS_MIDMAP T1\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT MER_NO, PUR_CD, VAN FROM TB_BAS_MERINFO\r\n");
			strbuf.append(")T5 ON(T1.MID=T5.MER_NO)\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT PUR_NM, PUR_CD, PUR_SORT FROM TB_BAS_PURINFO\r\n");
			strbuf.append(")T2 ON(T5.PUR_CD=T2.PUR_CD)\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART\r\n");
			strbuf.append(")T3 ON(T1.DEP_CD=T3.DEP_CD)\r\n");
			strbuf.append("WHERE \r\n");
			strbuf.append("T1.ORG_CD='"+orgcd+"' \r\n");
			strbuf.append("ORDER BY T1.DEP_CD ASC, T2.PUR_SORT ASC, INSTIME ASC\r\n");

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("DEP_NM",um.getString(rs.getString("DEP_NM")));
	            jsonob.put("PUR_NM",um.getString(rs.getString("PUR_NM")));
	            jsonob.put("MID",um.getString(rs.getString("MID")));
	            jsonob.put("VAN",um.getString(rs.getString("VAN")));
	            jsonob.put("INSUSER",um.getString(rs.getString("INSUSER")));
	            jsonob.put("INS_DT",um.getString(rs.getString("INS_DT")));
	            jsonary.add(jsonob);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}
	
	/**
	 * �뜝�뙟紐뚯삕�뜝�룞�삕�뜝�떕占� �뜝�룞�삕�뜝�룞�삕
	 * @param orgcd
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0605_01(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray jsonary = new JSONArray();

		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n");
			strbuf.append("TID_CD\r\n");
			strbuf.append(", ORG_CD\r\n");
			strbuf.append(", T1.DEP_CD\r\n");
			strbuf.append(", DEP_NM\r\n");
			strbuf.append(", TERM_NM\r\n");
			strbuf.append(", TERM_ID\r\n");
			strbuf.append(", TERM_TYPE\r\n");
			strbuf.append(", VAN\r\n");
			strbuf.append(", TO_CHAR(TERM_IST_DD,'YYYY-MM-DD HH24:MI:SS') INS_DT\r\n");
			strbuf.append("FROM\r\n");
			strbuf.append("TB_BAS_TIDMST T1\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART\r\n");
			strbuf.append(")T2 ON(T1.DEP_CD=T2.DEP_CD)\r\n");
			strbuf.append("WHERE \r\n");
			strbuf.append("ORG_CD='"+orgcd+"'\r\n");
			strbuf.append("ORDER BY T1.DEP_CD ASC, T1.TERM_IST_DD ASC\r\n");


			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("DEP_NM",um.getString(rs.getString("DEP_NM")));
	            jsonob.put("TERM_NM",um.getString(rs.getString("TERM_NM")));
	            jsonob.put("TERM_ID",um.getString(rs.getString("TERM_ID")));
	            jsonob.put("VAN",um.getString(rs.getString("VAN")));
	            jsonob.put("TERM_TYPE",um.getString(rs.getString("TERM_TYPE")));
	            jsonob.put("INS_DT",um.getString(rs.getString("INS_DT")));
	            jsonary.add(jsonob);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}

	/**
	 * �뜝�뙟紐뚯삕�뜝�룞�삕�뜝�떕占� �뜝�룞�삕�뜝�룞�삕�뜝�떕占�
	 * @param orgcd
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONArray get_sub0605_02(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray jsonary = new JSONArray();

		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT\r\n");
			strbuf.append("ORG_CD,\r\n");
			strbuf.append("T1.DEP_CD,\r\n");
			strbuf.append("T3.DEP_NM,\r\n");
			strbuf.append("TID,\r\n");
			strbuf.append("VAN,\r\n");
			strbuf.append("TO_CHAR(INSTIME,'YYYY-MM-DD HH24:MI:SS') INS_DT,\r\n");
			strbuf.append("INSUSER,\r\n");
			strbuf.append("TERM_NM\r\n");
			strbuf.append("FROM\r\n");
			strbuf.append("TB_BAS_TIDMAP T1\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT TERM_ID, VAN, TERM_NM FROM TB_BAS_TIDMST WHERE ORG_CD='"+orgcd+"'\r\n");
			strbuf.append(")T5 ON(T1.TID=T5.TERM_ID)\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='"+orgcd+"'\r\n");
			strbuf.append(")T3 ON(T1.DEP_CD=T3.DEP_CD)\r\n");
			strbuf.append("WHERE \r\n");
			strbuf.append("T1.ORG_CD='"+orgcd+"' \r\n");
			strbuf.append("ORDER BY T1.DEP_CD ASC, INSTIME ASC\r\n");



			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("DEP_NM",um.getString(rs.getString("DEP_NM")));
	            jsonob.put("TERM_NM",um.getString(rs.getString("TERM_NM")));
	            jsonob.put("TID",um.getString(rs.getString("TID")));
	            jsonob.put("VAN",um.getString(rs.getString("VAN")));
	            jsonob.put("INSUSER",um.getString(rs.getString("INSUSER")));
	            jsonob.put("INS_DT",um.getString(rs.getString("INS_DT")));
	            jsonary.add(jsonob);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonary;
	}

	/**
	 * �쉶�뜝�룞�삕 �뜝�뙥怨ㅼ삕
	 * @param user_id,program_seq,sort
	 * @return 1 : �뜝�룞�삕�뜝�룞�삕 / 0 : �뜝�룞�삕�뜝�룞�삕 (�뜝�떥�눦�삕�듃�뜝�룞�삕 �뜝�떥�슱�삕 �뜝�룞�삕)
	 * 2023-04-05 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public int register_user(String depcd,String orgcd, String user_id, String user_pw, String user_lv, String user_tel1, String user_tel2, String user_name, String user_email, String ins_id) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Date date = new Date();
		String memcd = "NMEM" + date.getTime();
		String auth_seq = (user_lv.equals("M")) ? "AS000001" : "AS000002"; 
		try {
			strbuf = new StringBuffer();
			strbuf.append("BEGIN INSERT INTO TB_BAS_USER(MEM_CD,DEP_CD,ORG_CD,AUTH_SEQ,USER_ID,USER_PW,USER_TEL1,USER_TEL2,USER_LV,USER_NM,USER_EMAIL,USER_FAX,INS_DT) VALUES(\r\n");
			strbuf.append("'"+memcd+"',\r\n");
			strbuf.append("'"+depcd+"',\r\n");
			strbuf.append("'"+orgcd+"',\r\n");
			strbuf.append("'"+auth_seq+"',\r\n");
			strbuf.append("'"+user_id+"',\r\n");
			strbuf.append("'"+user_pw+"',\r\n");
			strbuf.append("'"+user_tel1+"',\r\n");
			strbuf.append("'"+user_tel2+"',\r\n");
			strbuf.append("'"+user_lv+"',\r\n");
			strbuf.append("'"+user_name+"',\r\n");
			strbuf.append("'"+user_email+"',\r\n");
			strbuf.append("'"+ins_id+"',\r\n");
			strbuf.append("SYSDATE);\r\n");
			strbuf.append("COMMIT; END;\r\n");
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			int rowsInserted = stmt.executeUpdate();
			
            if (rowsInserted > 0) {
                return rowsInserted;
            } else {
                return 0;
            }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return 0;
	}
	
	
	/**
	 * �뜝�룞�삕�뜝�떛�벝�삕 �뜝�뙥釉앹삕
	 * @param user_id
	 * @return 1�뜝�룞�삕�뜝�룞�삕�겕�뜝�룞�삕 : �뜝�뙥釉앹삕 / 0 : �뜝�뙥釉앹삕�뜝�룞�삕�뜝�룞�삕 (�뜝�떥�눦�삕�듃�뜝�룞�삕 �뜝�떥�슱�삕 �뜝�룞�삕)
	 * 2023-04-05 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public int isIdDuplicated (String user_id) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT COUNT(1) CNT FROM TB_BAS_USER WHERE\r\n");
			strbuf.append("USER_ID = '"+user_id+"'\r\n");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			rs = stmt.executeQuery();
			if(rs.next()) {
			return rs.getInt("CNT");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return 0;
	}
	

	/**
	 * �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕�뜝�룞�삕_�뜝�룞�삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕�뜝�룞�삕
	 * @param orgcd
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public JSONObject get_sub0602detail_user(String orgcd,String memcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONObject jsonob = new JSONObject();

		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT \r\n");
			strbuf.append("MEM_CD\r\n");
			strbuf.append(", ORG_CD\r\n");
			strbuf.append(", T1.DEP_CD\r\n");
			strbuf.append(", DEP_NM\r\n");
			strbuf.append(", AUTH_SEQ\r\n");
			strbuf.append(", USER_ID\r\n");
			strbuf.append(", USER_PW\r\n");
			strbuf.append(", USER_NM\r\n");
			strbuf.append(", USER_TEL1\r\n");
			strbuf.append(", USER_TEL2\r\n");
			strbuf.append(", USER_MEMO\r\n");
			strbuf.append(", USER_EMAIL\r\n");
			strbuf.append(", USER_FAX\r\n");
			strbuf.append(", USER_LV\r\n");
			strbuf.append(", TO_CHAR(INS_DT,'YYYY-MM-DD HH24:MI:SS') INS_DT\r\n");
			strbuf.append("FROM\r\n");
			strbuf.append("TB_BAS_USER T1\r\n");
			strbuf.append("LEFT OUTER JOIN(\r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART\r\n");
			strbuf.append(")T2 ON(T1.DEP_CD=T2.DEP_CD)\r\n");
			strbuf.append("WHERE \r\n");
			strbuf.append("ORG_CD='"+orgcd+"' AND MEM_CD = '"+memcd+"'\r\n");
			strbuf.append("ORDER BY USER_ID ASC, INS_DT ASC\r\n");


			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rs = stmt.executeQuery();
			while(rs.next()) {
	            jsonob.put("MEM_CD",um.getString(rs.getString("MEM_CD")));
	            jsonob.put("ORG_CD",um.getString(rs.getString("ORG_CD")));
	            jsonob.put("DEP_CD",um.getString(rs.getString("DEP_CD")));
	            jsonob.put("DEP_NM",um.getString(rs.getString("DEP_NM")));
	            jsonob.put("AUTH_SEQ",um.getString(rs.getString("AUTH_SEQ")));
	            jsonob.put("USER_ID",um.getString(rs.getString("USER_ID")));
	            jsonob.put("USER_NM",um.getString(rs.getString("USER_NM")));
	            jsonob.put("USER_TEL1",um.getString(rs.getString("USER_TEL1")));
	            jsonob.put("USER_TEL2",um.getString(rs.getString("USER_TEL2")));
	            jsonob.put("USER_EMAIL",um.getString(rs.getString("USER_EMAIL")));
	            jsonob.put("INS_DT",um.getString(rs.getString("INS_DT")));
	            jsonob.put("USER_LV",um.getString(rs.getString("USER_LV")));
	            jsonob.put("INS_ID",um.getString(rs.getString("USER_FAX")));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return jsonob;
	}
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public int modify_user(String memcd, String user_pw, String user_nm, String user_email, String user_tel1, String user_tel2, String user_lv) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int rowsInserted = 0;
		try {
			strbuf = new StringBuffer();
			strbuf.append("BEGIN \r\n");
			strbuf.append("UPDATE TB_BAS_USER SET\r\n");
			strbuf.append("USER_NM = '"+user_nm+"' \r\n");
			if(!Objects.equals(user_pw, "")) strbuf.append(",USER_PW = '"+user_pw+"' \r\n");
			strbuf.append(",USER_EMAIL = '"+user_email+"' \r\n");
			strbuf.append(",USER_TEL1 = '"+user_tel1+"' \r\n");
			strbuf.append(",USER_TEL2 = '"+user_tel2+"' \r\n");
			strbuf.append(",USER_LV = '"+user_lv+"' \r\n");
			strbuf.append("WHERE MEM_CD = '"+memcd+"'; \r\n");
			strbuf.append("COMMIT; END; \r\n");

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rowsInserted = stmt.executeUpdate();
					

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return rowsInserted;
	}
	
	/**
	 * �뜝�룞�삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕 �뜝�룞�삕�뜝�룞�삕
	 * @return ...
	 * 2023-04-03 �뜝�룞�삕�뜝�듅源띿삕
	 */
	public int delete_user(String memcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int rowsInserted = 0;
		try {
			strbuf = new StringBuffer();
			strbuf.append("BEGIN \r\n");
			strbuf.append("DELETE FROM TB_BAS_USER WHERE\r\n");
			strbuf.append("MEM_CD = '"+memcd+"';\r\n");
			strbuf.append("COMMIT; END; \r\n");

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			rowsInserted = stmt.executeUpdate();
					

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return rowsInserted;
	}
	
	
	public int insert_icvan(HashMap<String, String> whereqry, String DEBUG) {

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
				util_manager util = new util_manager();
				List<String> resultList = util.insert_query(whereqry);
				
				String orgcd = resultList.get(0);
				String depcd_where = resultList.get(1);
				String set_icvan = resultList.get(2);
				String set_depdata = resultList.get(3);
		
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("INSERT INTO GLOB_MNG_ICVAN ( \r\n");
			strbuf.append("SEQNO, BIZNO, TID, MID, VANGB, MDATE, SVCGB, TRANIDX, APPGB, ENTRYMD, \r\n");			
			strbuf.append("APPDD, APPTM, APPNO, CARDNO, HALBU, CURRENCY, AMOUNT, AMT_UNIT, AMT_TIP, AMT_TAX, \r\n");
			strbuf.append("ISS_CD, ISS_NM, ACQ_CD, ACQ_NM, AUTHCD, AUTHMSG, CARD_CODE, CHECK_CARD, OVSEA_CARD, TLINEGB,  \r\n");			
			strbuf.append("SIGNCHK, DDCGB, EXT_FIELD, OAPPNO, OAPPDD, OAPPTM, OAPP_AMT, ADD_GB, ADD_CID, ADD_CD,  \r\n"); 			
			strbuf.append("ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, SECTION_NO, SERVID, DPFLAG, DEPOREQDD, REQDEPTH, TRAN_STAT,  \r\n"); 
			strbuf.append("DEPOSEQ, CTR_RST, CTR_DT, ADD_DEPT, MEDI_GOODS  \r\n"); 
			strbuf.append(" ) VALUES( " + set_icvan + ") \r\n"); 		
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			//if(Objects.equals(DEBUG,"Y")) {
						
			int rowsInserted = stmt.executeUpdate();
			
            if (rowsInserted > 0) {
                return rowsInserted;
            } else {
                return 0;
            }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return 0;
	}
	
	
	public int insert_depdata(JSONArray jsonArray, String DEBUG, HashMap<String, String> whereqry) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		///WHERE QRY///
				util_manager util = new util_manager();
				List<String> resultList = util.insert_query(whereqry);
				
				String orgcd = resultList.get(0);
				String depcd_where = resultList.get(1);
				String set_icvan = resultList.get(2);
				String set_depdata = resultList.get(3);
		
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("INSERT INTO TB_MNG_DEPDATA ( \r\n");
			strbuf.append("DEP_SEQ, VAN_GB, DEP_CD, FILE_DD, EXP_DD, MID, CO_TYPE, REQ_DD, RTN_CD, APP_DD, \r\n");
			strbuf.append("REG_DD, CARD_NO, HALBU, SALE_AMT, RSC_CD, RSV_CD, FEE, RS_MSG, APP_NO, FILE_NM, \r\n");
			strbuf.append("TID, TRANIDX, ACQ_CD, ISS_CD, COM_NO, OAPP_DD, AMT_TIP, AMT_TAX, PROC_GB, PROC_DD, \r\n");
			strbuf.append("DP_CLID, CARDTP, ORGCD, SYSTYPE  \r\n"); 
			strbuf.append(" ) VALUES( " + set_depdata + ") \r\n"); 		
		
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			
			
			int rowsInserted = stmt.executeUpdate();
			
            if (rowsInserted > 0) {
                return rowsInserted;
            } else {
                return 0;
            }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return 0;
	}
	
	public int update_icvan(HashMap<String, String> whereqry, String DEBUG) {

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		JSONArray jsonary = new JSONArray();
		
		///WHERE QRY///
				util_manager util = new util_manager();
				List<String> resultList = util.insert_query(whereqry);
				
				String orgcd = resultList.get(0);
				String depcd_where = resultList.get(1);
				String set_icvan = resultList.get(2);
				String set_depdata = resultList.get(3);
				String update_icvan = resultList.get(4);
		
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("UPDATE GLOB_MNG_ICVAN SET  \r\n");
			strbuf.append(" " + update_icvan +" \r\n"); 		
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			
			System.out.println(strbuf.toString());	//�뜝�떥源띿삕
			//if(Objects.equals(DEBUG,"Y")) {
						
			int rowsInserted = stmt.executeUpdate();
			
            if (rowsInserted > 0) {
                return rowsInserted;
            } else {
                return 0;
            }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setOraClose(con,stmt,rs);
		}
		return 0;
	}
	
	
	
}//end class
