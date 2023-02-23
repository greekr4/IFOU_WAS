package com.gaon.nifou.v3;

import java.beans.Statement;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
	
	//DB연결 정보
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
	 * 유저 Select Query
	 * @param uid : 유저아이디
	 * @return count(1)
	 * 2023-02-01 김태균
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
			stmt.setString(1, uid); //유저 ID
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
	 * 유저 정보 Select Query
	 * @param uid : 유저아이디
	 * @return USER_PW, DEP_CD, ORG_CD, USER_LV
	 * 2023-02-01 김태균
	 */
	public String[] get_user_info(String uid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String[] rtnstr = new String[5];
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT USER_PW, DEP_CD, ORG_CD, USER_LV, AUTH_SEQ FROM TB_BAS_USER WHERE USER_ID=?");

			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			stmt.setString(1, uid); //유저 ID

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
	 * @param uid : 유저아이디
	 * @return "ID|ORG_CD|DEP_CD|ORG_NO|PTAB|VTAB|DTAB|USER_LV|TRANS_NO "
	 * 2023-02-01 김태균
	 */
	public String[] get_user_uauth(String uid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String[] rtnstr = new String[10];
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT t2.USER_ID, t2.ORG_CD, t2.DEP_CD , t1.ORG_NO , t1.PTAB , t1.VTAB , t1.DTAB , t2.USER_LV,t2.TRANS_NO,t2.AUTH_SEQ FROM TB_BAS_ORG t1 ");
			strbuf.append("INNER JOIN TB_BAS_USER t2 ");
			strbuf.append("ON (t1.ORG_CD=t2.ORG_CD) ");
			strbuf.append("where t2.USER_ID = ?");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString()); //로그
			stmt.setString(1, uid); //유저 ID

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
	 * @param uid : 유저아이디
	 * @return trans_no
	 * 2023-02-01 김태균
	 */
	public String get_user_trans_no(String uid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String rtnstr = "";
		try {
			strbuf = new StringBuffer();
			strbuf.append("select trans_no from tb_bas_user where user_id = ?");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, uid); //유저 ID
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
	 * dhx 컬럼 Select Query
	 * @param orgcd,pages
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-01 김태균
	 */
	public JSONArray get_tb_sys_domain(String orgcd,String pages) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("select * from tb_sys_domain where orgcd = ? and pages = ? order by orn asc");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, orgcd); //유저 ID
			stmt.setString(2, pages); //유저 ID
			
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
	 * dhx 컬럼 Select Query 필수값조회
	 * @param orgcd,pages
	 * @return json형식으로 dhx형식에 맞게 (검색 컬럼확인 용)
	 * 2023-02-01 김태균
	 */
	public JSONArray get_tb_sys_domain_sel(String orgcd,String pages) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("select * from tb_sys_domain where orgcd = ? and pages = ? order by orn asc");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, orgcd); //유저 ID
			stmt.setString(2, pages); //유저 ID
			
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-01 김태균
	 */
	public JSONArray get_sub0201(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			
			strbuf.append("SELECT \r\n");
			strbuf.append("SEQNO, \r\n");
			strbuf.append("	APPGB TR_AUTHTXT,\r\n");
			strbuf.append("	DEP_NM		TR_DEPNM, \r\n");
			strbuf.append("	TERM_NM		TR_TIDNM, \r\n");
			strbuf.append("	TID			TR_TID, \r\n");
			strbuf.append("	MID			TR_MID, \r\n");
			strbuf.append("	PUR_NM		TR_ACQNM, \r\n");
			strbuf.append("	APPDD		TR_APPDD,\r\n");
			strbuf.append("	APPTM		TR_APPTM,\r\n");
			strbuf.append("	OAPPDD		TR_OAPPDD,\r\n");
			strbuf.append("	APPNO		TR_APPNO, \r\n");
			strbuf.append("	APPGB_TXT	TR_AUTHSTAT, \r\n");
			strbuf.append("	CARDNO		TR_CARDNO,	\r\n");
			strbuf.append("	AMOUNT		TR_AMT,	\r\n");
			strbuf.append("	HALBU		TR_HALBU, \r\n");
			strbuf.append("	CARDTP_TXT	TR_CARDTP, \r\n");
			strbuf.append("	TLINEGBTXT	TR_LINE,\r\n");
			strbuf.append("	SIGNCHK_TXT TR_SIGN,\r\n");
			strbuf.append("	AUTHCD		TR_RST_CD,\r\n");
			strbuf.append("	DEPO_DD		DP_REQ_DD,\r\n");
			strbuf.append("	REQ_DD		DP_RES_DD,	\r\n");
			strbuf.append("	REG_DD		DP_REG_DD,\r\n");
			strbuf.append("	RTN_TXT		DP_RST_TXT,\r\n");
			strbuf.append("	EXP_DD		DP_EXP_DD,\r\n");
			strbuf.append("	ADD_CID		ADD_PID,\r\n");
			strbuf.append("	ADD_GB		ADD_PGB,\r\n");
			strbuf.append("	ADD_CASHER	ADD_CID,\r\n");
			strbuf.append("	TRANIDX		TR_SEQNO,\r\n");
			strbuf.append("	ADD_RECP,\r\n");
			strbuf.append("	AUTHMSG		TR_RST_MSG,\r\n");
			strbuf.append(" OAPP_AMT    TR_CANDD,\r\n");
			strbuf.append(" '정상거래'    APP_MSG,\r\n");
			strbuf.append(" '국내카드'    OVSEA_CARD,\r\n");
			strbuf.append(" '진료과'      ADD_RHK\r\n");
			strbuf.append("FROM(\r\n");
			strbuf.append("	SELECT\r\n");
			strbuf.append("		SEQNO, DEP_NM, TERM_NM, TID, MID, PUR_NM,\r\n");
			strbuf.append("		APPDD, APPTM,  OAPPDD, APPNO, OAPP_AMT,APPGB,\r\n");
			strbuf.append("		CASE \r\n");
			strbuf.append("			--승인거래\r\n");
			strbuf.append("			WHEN APPGB='A' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0015')\r\n");
			strbuf.append("			--취소거래\r\n");
			strbuf.append("			WHEN APPGB='C' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0016')\r\n");
			strbuf.append("		END APPGB_TXT,\r\n");
			strbuf.append("		CARDNO,	AMOUNT,	HALBU,\r\n");
			strbuf.append("		CASE \r\n");
			strbuf.append("			--체크카드\r\n");
			strbuf.append("			WHEN CHECK_CARD='Y' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0019') \r\n");
			strbuf.append("			--신용카드\r\n");
			strbuf.append("			ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0018') END CARDTP_TXT,\r\n");
			strbuf.append("		CASE\r\n");
			strbuf.append("			--전자서명\r\n");
			strbuf.append("			WHEN SIGNCHK='1' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0021') \r\n");
			strbuf.append("			--무서명\r\n");
			strbuf.append("			ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0022') END SIGNCHK_TXT,\r\n");
			strbuf.append("		REQ_DD,	AUTHCD,	REG_DD,	RTN_CD,\r\n");
			strbuf.append("		CASE\r\n");
			strbuf.append("			--결과없음\r\n");
			strbuf.append("			WHEN RTN_CD IS NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0024') \r\n");
			strbuf.append("			--정상매입\r\n");
			strbuf.append("			WHEN RTN_CD IN('60', '67') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0025')\r\n");
			strbuf.append("			--매입반송\r\n");
			strbuf.append("			WHEN RTN_CD IN('61', '64') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0026') \r\n");
			strbuf.append("		END RTN_TXT,\r\n");
			strbuf.append("		EXP_DD,	EXT_FIELD,	TRANIDX, AUTHMSG\r\n");
			strbuf.append("		,CASE WHEN TLINEGB IS NOT NULL THEN (SELECT CODE_VAL FROM TB_BAS_CODE WHERE TRIM(CODE_NO)=TRIM(TLINEGB)) END TLINEGBTXT\r\n");
			strbuf.append("		,ADD_GB, ADD_CID, ADD_CD, ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, DEPO_DD\r\n");
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
			strbuf.append("			SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='OR008'\r\n");
			strbuf.append("		)T3 ON(T1.TID=T3.TERM_ID)\r\n");
			strbuf.append("		LEFT OUTER JOIN( \r\n");
			strbuf.append("			SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='OR008'\r\n");
			strbuf.append("		)T4 ON(T3.DEP_CD=T4.DEP_CD)\r\n");
			strbuf.append("		LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_CD FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_CD)\r\n");
			strbuf.append("		WHERE SVCGB IN ('CC', 'CE')  AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where org_cd='OR008' AND dep_cd='DP30101')   AND T1.APPDD>='20230203' and T1.APPDD<='20230203'\r\n");
			strbuf.append("		order by appdd desc, apptm desc\r\n");
			strbuf.append("	)\r\n");
			strbuf.append(")\r\n");
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-01 김태균
	 */
	public JSONArray get_sub0201T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			
			strbuf.append("SELECT\r\n"
					+ "  DEP_NM\r\n"
					+ "	,TERM_ID\r\n"
					+ "	,TERM_NM\r\n"
					+ "    ,ACNT\r\n"
					+ "    ,CCNT\r\n"
					+ "    ,AAMT\r\n"
					+ "    ,CAMT\r\n"
					+ "    ,TOTCNT\r\n"
					+ "    ,TOTAMT\r\n"
					+ "    ,BC\r\n"
					+ "    ,NH\r\n"
					+ "    ,KB\r\n"
					+ "    ,SS\r\n"
					+ "    ,HN\r\n"
					+ "    ,LO\r\n"
					+ "    ,HD\r\n"
					+ "    ,SI\r\n"
					+ "	,GD\r\n"
					+ ",'0' ZERO,'0' KAKAO,'0' HN, '0' JH, '0' AP, '0' WP"
					+ "\r\n"					
					+ "FROM(    \r\n"
					+ "    SELECT\r\n"
					+ "         TID\r\n"
					+ "        ,SUM(ACNT) ACNT\r\n"
					+ "        ,SUM(CCNT) CCNT\r\n"
					+ "        ,SUM(AAMT) AAMT\r\n"
					+ "        ,SUM(CAMT) CAMT\r\n"
					+ "        ,SUM(ACNT)+SUM(CCNT) TOTCNT\r\n"
					+ "        ,SUM(AAMT)-SUM(CAMT) TOTAMT\r\n"
					+ "        ,SUM(ABC  )-SUM(CBC  ) BC\r\n"
					+ "        ,SUM(ANH  )-SUM(CNH  ) NH\r\n"
					+ "        ,SUM(AKB  )-SUM(CKB  ) KB\r\n"
					+ "        ,SUM(ASS  )-SUM(CSS  ) SS\r\n"
					+ "        ,SUM(AHN  )-SUM(CHN  ) HN\r\n"
					+ "        ,SUM(ALO  )-SUM(CLO  ) LO\r\n"
					+ "        ,SUM(AHD  )-SUM(CHD  ) HD\r\n"
					+ "        ,SUM(ASI  )-SUM(CSI  ) SI\r\n"
					+ "		,SUM(AGD  )-SUM(CGD  ) GD\r\n"
					+ "    FROM(    \r\n"
					+ "        SELECT\r\n"
					+ "            TID\r\n"
					+ "            ,CASE WHEN APPGB='A' THEN COUNT(1) ELSE 0 END ACNT\r\n"
					+ "            ,CASE WHEN APPGB='C' THEN COUNT(1) ELSE 0 END CCNT\r\n"
					+ "            ,CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT\r\n"
					+ "            ,CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0006', '026', '1106') THEN SUM(AMOUNT) ELSE 0 END ABC\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0030', '018', '2211') THEN SUM(AMOUNT) ELSE 0 END ANH\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0001', '016', '1101') THEN SUM(AMOUNT) ELSE 0 END AKB\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0004', '031', '1104') THEN SUM(AMOUNT) ELSE 0 END ASS\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0005', '008', '1105') THEN SUM(AMOUNT) ELSE 0 END AHN\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0003', '047', '1103') THEN SUM(AMOUNT) ELSE 0 END ALO\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0002', '027', '1102') THEN SUM(AMOUNT) ELSE 0 END AHD\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0007', '029', '1107') THEN SUM(AMOUNT) ELSE 0 END ASI\r\n"
					+ "            ,CASE WHEN APPGB='A' AND ACQ_CD IN ('VC0008', '013', '1113') THEN SUM(AMOUNT) ELSE 0 END AGD\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0006', '026', '1106') THEN SUM(AMOUNT) ELSE 0 END CBC\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0030', '018', '2211') THEN SUM(AMOUNT) ELSE 0 END CNH\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0001', '016', '1101') THEN SUM(AMOUNT) ELSE 0 END CKB\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0004', '031', '1104') THEN SUM(AMOUNT) ELSE 0 END CSS\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0005', '008', '1105') THEN SUM(AMOUNT) ELSE 0 END CHN\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0003', '047', '1103') THEN SUM(AMOUNT) ELSE 0 END CLO\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0002', '027', '1102') THEN SUM(AMOUNT) ELSE 0 END CHD\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0007', '029', '1107') THEN SUM(AMOUNT) ELSE 0 END CSI\r\n"
					+ "            ,CASE WHEN APPGB='C' AND ACQ_CD IN ('VC0008', '013', '1113') THEN SUM(AMOUNT) ELSE 0 END CGD\r\n"
					+ "        FROM (\r\n"
					+ "             SELECT\r\n"
					+ "				SEQNO, DEP_NM, TERM_NM, TID, MID, PUR_NM, ACQ_CD, \r\n"
					+ "				APPGB_TXT, APPDD, APPTM, OAPPDD, APPNO, APPGB,\r\n"
					+ "				TR_AUTHSTAT, CARDNO, AMOUNT, HALBU, CARDTP_TXT, SIGNCHK_TXT,\r\n"
					+ "				REQ_DD,	AUTHCD,	REG_DD,	RTN_CD, RTN_TXT, \r\n"
					+ "				EXP_DD,	EXT_FIELD,	TRANIDX, AUTHMSG\r\n"
					+ "			FROM(\r\n"
					+ "				SELECT\r\n"
					+ "					RNUM, SEQNO, DEP_NM, TERM_NM, TID, MID, PUR_NM, APPGB,\r\n"
					+ "					CASE \r\n"
					+ "					--정상거래\r\n"
					+ "					WHEN APPGB='A' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0011')\r\n"
					+ "					--취소거래\r\n"
					+ "					WHEN APPGB='C' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0012')\r\n"
					+ "				END APPGB_TXT,\r\n"
					+ "				APPDD, APPTM, OAPPDD, APPNO, ACQ_CD,\r\n"
					+ "				CASE \r\n"
					+ "					--승인거래\r\n"
					+ "					WHEN APPGB='A' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0015')\r\n"
					+ "					--취소거래\r\n"
					+ "					WHEN APPGB='C' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0016')\r\n"
					+ "				END TR_AUTHSTAT,\r\n"
					+ "				CARDNO,	AMOUNT,	HALBU,\r\n"
					+ "				CASE \r\n"
					+ "					--체크카드\r\n"
					+ "					WHEN CHECK_CARD='Y' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0019') \r\n"
					+ "					--신용카드\r\n"
					+ "					ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0018') \r\n"
					+ "				END CARDTP_TXT,\r\n"
					+ "				CASE\r\n"
					+ "					--전자서명\r\n"
					+ "					WHEN SIGNCHK='1' THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0021') \r\n"
					+ "					--무서명\r\n"
					+ "					ELSE (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0022') \r\n"
					+ "				END SIGNCHK_TXT,\r\n"
					+ "				REQ_DD,	AUTHCD,	REG_DD,	RTN_CD,\r\n"
					+ "				CASE\r\n"
					+ "					--결과없음\r\n"
					+ "					WHEN RTN_CD IS NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0024') \r\n"
					+ "					--정상매입\r\n"
					+ "					WHEN RTN_CD IN('60', '67') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0025')\r\n"
					+ "					--매입반송\r\n"
					+ "					WHEN RTN_CD IN('61', '64') THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR008' AND SCD_CD='SCD0026') \r\n"
					+ "				END RTN_TXT,\r\n"
					+ "					EXP_DD,	EXT_FIELD,	TRANIDX, AUTHMSG\r\n"
					+ "				FROM(\r\n"
					+ "					SELECT\r\n"
					+ "						ROWNUM AS RNUM,\r\n"
					+ "						SEQNO, BIZNO, TID, MID, VANGB, MDATE, SVCGB, T1.TRANIDX, T1.APPGB, ENTRYMD,\r\n"
					+ "						T1.APPDD, APPTM, T1.APPNO, T1.CARDNO, HALBU, CURRENCY, T1.AMOUNT, AMT_UNIT, AMT_TIP, AMT_TAX,\r\n"
					+ "						ISS_CD, ISS_NM, ACQ_CD, ACQ_NM, AUTHCD, AUTHMSG, CARD_CODE, CHECK_CARD, OVSEA_CARD, TLINEGB,\r\n"
					+ "						SIGNCHK, DDCGB, EXT_FIELD, OAPPNO, OAPPDD, OAPPTM, OAPP_AMT, ADD_GB, ADD_CID, ADD_CD,\r\n"
					+ "						ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, SECTION_NO, PUR_NM, DEP_NM, EXP_DD, REQ_DD, REG_DD, RSC_CD, RTN_CD, TERM_NM\r\n"
					+ "					FROM\r\n"
					+ "						GLOB_MNG_ICVAN T1\r\n"
					+ "					LEFT OUTER JOIN(\r\n"
					+ "						SELECT EXP_DD, REQ_DD, REG_DD, APP_DD, TRANIDX, RSC_CD, RTN_CD FROM TB_MNG_DEPDATA\r\n"
					+ "					)T2 ON(T1.APPDD=T2.APP_DD AND T1.TRANIDX=T2.TRANIDX)\r\n"
					+ "					LEFT OUTER JOIN( SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='OR008')T3 ON(T1.TID=T3.TERM_ID)\r\n"
					+ "					LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='OR008')T4 ON(T3.DEP_CD=T4.DEP_CD)\r\n"
					+ "					LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_KOCES FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES)\r\n"
					+ "					WHERE SVCGB IN ('CC', 'CE')  AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where org_cd='OR008' AND dep_cd='DP30101')   AND T1.APPDD>='20220203' and T1.APPDD<='20230203' AND ROWNUM <= (200*1)\r\n"
					+ "					order by appdd desc, apptm desc\r\n"
					+ "				)  WHERE RNUM >= (200*(1-1)+1) \r\n"
					+ "			)\r\n"
					+ "			\r\n"
					+ "		)\r\n"
					+ "		GROUP BY TID, APPGB, ACQ_CD\r\n"
					+ "    )\r\n"
					+ "    GROUP BY TID        \r\n"
					+ ")T2\r\n"
					+ "LEFT OUTER JOIN( SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='OR008')T3 ON(T2.TID=T3.TERM_ID)\r\n"
					+ "LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='OR008')T4 ON(T3.DEP_CD=T4.DEP_CD)");
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0202(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력

			strbuf.append("SELECT \r\n");
			strbuf.append("		DEP_NM, PUR_NM, T1.MID, ACQ_CD, APPDD TR_APPDD,ACNT+CCNT TOTCNT,AAMT-CAMT TOTAMT, ACNT, CCNT, AAMT, CAMT \r\n");
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
			strbuf.append("				WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where org_cd='OR026') AND APPDD LIKE '202302%' \r\n");
			strbuf.append("				GROUP BY MID, ACQ_CD, APPDD, APPGB \r\n");
			strbuf.append("				)T1 \r\n");
			strbuf.append("			GROUP BY MID, ACQ_CD, APPDD \r\n");
			strbuf.append("			ORDER BY MID \r\n");
			strbuf.append("			)T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("		SELECT MER_NO, PUR_CD, DEP_CD FROM TB_BAS_MERINFO WHERE ORG_CD='OR026' \r\n");
			strbuf.append("		)TM ON(T1.MID=TM.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='OR026')T4 ON(TM.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN(  \r\n");
			strbuf.append("		SELECT PUR_NM, PUR_KOCES, PUR_SORT,PUR_CD, PUR_OCD FROM TB_BAS_PURINFO \r\n");
			strbuf.append("		)T5 ON(T1.ACQ_CD=T5.PUR_KOCES OR T1.ACQ_CD=T5.PUR_OCD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("		SELECT ORG_CD, USER_PUR_CD, USER_PURSORT FROM TB_BAS_USERPURINFO WHERE ORG_CD='OR026' \r\n");
			strbuf.append("		)S3 ON(T5.PUR_CD=S3.USER_PUR_CD) \r\n");
			strbuf.append("ORDER BY APPDD ASC, DEP_NM ASC, PUR_NM ASC \r\n");			
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0203(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT  \r\n");
			strbuf.append("APPDD TR_APPDD  \r\n");
			strbuf.append(",ADD_CASHER ADD_CID  \r\n");
			strbuf.append(",SUM(BC_CNT) BC_CNT  \r\n");
			strbuf.append(",SUM(BCA)-SUM(BCC) BC_AMT  \r\n");
			strbuf.append(",SUM(NH_CNT) NH_CNT  \r\n");
			strbuf.append(",SUM(NHA)-SUM(NHC) NH_AMT  \r\n");
			strbuf.append(",SUM(KB_CNT) KB_CNT  \r\n");
			strbuf.append(",SUM(KBA)-SUM(KBC) KB_AMT  \r\n");
			strbuf.append(",SUM(SS_CNT) SS_CNT  \r\n");
			strbuf.append(",SUM(SSA)-SUM(SSC) SS_AMT  \r\n");
			strbuf.append(",SUM(HN_CNT) HN_CNT  \r\n");
			strbuf.append(",SUM(HNA)-SUM(HNC) HN_AMT  \r\n");
			strbuf.append(",SUM(LO_CNT) LO_CNT  \r\n");
			strbuf.append(",SUM(LOA)-SUM(LOC) LO_AMT  \r\n");
			strbuf.append(",SUM(HD_CNT) HD_CNT  \r\n");
			strbuf.append(",SUM(HDA)-SUM(HDC) HD_AMT  \r\n");
			strbuf.append(",SUM(SI_CNT) SI_CNT  \r\n");
			strbuf.append(",SUM(SIA)-SUM(SIC) SI_AMT  \r\n");
			strbuf.append(",SUM(GD_CNT) JH_CNT  \r\n");
			strbuf.append(",SUM(GDA)-SUM(GDC) JH_AMT  \r\n");
			strbuf.append(",SUM(BC_CNT)+SUM(NH_CNT)+SUM(KB_CNT)+SUM(SS_CNT)+SUM(HN_CNT)+SUM(LO_CNT)+SUM(HD_CNT)+SUM(SI_CNT)+SUM(GD_CNT) TOTCNT  \r\n");
			strbuf.append(",SUM(BCA)-SUM(BCC)+SUM(NHA)-SUM(NHC)+SUM(KBA)-SUM(KBC)+SUM(SSA)-SUM(SSC)+SUM(HNA)-SUM(HNC)+SUM(LOA)-SUM(LOC)+SUM(HDA)-SUM(HDC)+SUM(SIA)-SUM(SIC)+SUM(GDA)-SUM(GDC) TOTAMT  \r\n");
			strbuf.append(",SUM(APA)-SUM(APC) AP_AMT  \r\n");
			strbuf.append(",SUM(AP_CNT) AP_CNT  \r\n");
			strbuf.append(",SUM(WPA)-SUM(WPC) WP_AMT  \r\n");
			strbuf.append(",SUM(WP_CNT) WP_CNT  \r\n");
			strbuf.append(",SUM(ZEROA)-SUM(ZEROC) ZERO_AMT  \r\n");
			strbuf.append(",SUM(ZERO_CNT) ZERO_CNT  \r\n");
			strbuf.append(",SUM(KAKAOA)-SUM(KAKAOC) KAKAO_AMT  \r\n");
			strbuf.append(",SUM(KAKAO_CNT) KAKAO_CNT  \r\n");
			strbuf.append(",SUM(CASHA)-SUM(CASHC) CASH_AMT  \r\n");
			strbuf.append(",SUM(CASH_CNT) CASH_CNT  \r\n");
			strbuf.append(",SUM(CASH_IC_A)-SUM(CASH_IC_C) CASH_IC_AMT  \r\n");
			strbuf.append(",SUM(CASH_IC_CNT) CASH_IC_CNT  \r\n");
			strbuf.append("FROM(  \r\n");
			strbuf.append("SELECT   \r\n");
			strbuf.append("ADD_CASHER  \r\n");
			strbuf.append(",APPDD  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('1106', '026') THEN SUM(AMOUNT) ELSE 0 END BCA  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('2211', '018') THEN SUM(AMOUNT) ELSE 0 END NHA  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('1101', '016') THEN SUM(AMOUNT) ELSE 0 END KBA  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('1104', '031') THEN SUM(AMOUNT) ELSE 0 END SSA  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('1105', '008') THEN SUM(AMOUNT) ELSE 0 END HNA  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('1103', '047') THEN SUM(AMOUNT) ELSE 0 END LOA  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('1102', '027') THEN SUM(AMOUNT) ELSE 0 END HDA  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('1107', '029') THEN SUM(AMOUNT) ELSE 0 END SIA  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('1113', '021') THEN SUM(AMOUNT) ELSE 0 END GDA  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('1106', '026') THEN SUM(AMOUNT) ELSE 0 END BCC  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('2211', '018') THEN SUM(AMOUNT) ELSE 0 END NHC  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('1101', '016') THEN SUM(AMOUNT) ELSE 0 END KBC  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('1104', '031') THEN SUM(AMOUNT) ELSE 0 END SSC  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('1105', '008') THEN SUM(AMOUNT) ELSE 0 END HNC  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('1103', '047') THEN SUM(AMOUNT) ELSE 0 END LOC  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('1102', '027') THEN SUM(AMOUNT) ELSE 0 END HDC  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('1107', '029') THEN SUM(AMOUNT) ELSE 0 END SIC  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('1113', '021') THEN SUM(AMOUNT) ELSE 0 END GDC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('1106', '026') THEN COUNT(1) ELSE 0 END BC_CNT  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('2211', '018') THEN COUNT(1) ELSE 0 END NH_CNT  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('1101', '016') THEN COUNT(1) ELSE 0 END KB_CNT  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('1104', '031') THEN COUNT(1) ELSE 0 END SS_CNT  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('1105', '008') THEN COUNT(1) ELSE 0 END HN_CNT  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('1103', '047') THEN COUNT(1) ELSE 0 END LO_CNT  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('1102', '027') THEN COUNT(1) ELSE 0 END HD_CNT  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('1107', '029') THEN COUNT(1) ELSE 0 END SI_CNT  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('1113', '021') THEN COUNT(1) ELSE 0 END GD_CNT  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('9999', '999') THEN SUM(AMOUNT) ELSE 0 END APA  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('9999', '999') THEN SUM(AMOUNT) ELSE 0 END APC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('9999', '999') THEN COUNT(1) ELSE 0 END AP_CNT  \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('9998', '998') THEN SUM(AMOUNT) ELSE 0 END WPA  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('9998', '998') THEN SUM(AMOUNT) ELSE 0 END WPC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('9998', '998') THEN COUNT(1) ELSE 0 END WP_CNT \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('9997', '997') THEN SUM(AMOUNT) ELSE 0 END ZEROA  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('9997', '997') THEN SUM(AMOUNT) ELSE 0 END ZEROC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('9997', '997') THEN COUNT(1) ELSE 0 END ZERO_CNT \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('9996', '996') THEN SUM(AMOUNT) ELSE 0 END KAKAOA  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('9996', '996') THEN SUM(AMOUNT) ELSE 0 END KAKAOC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('9996', '996') THEN COUNT(1) ELSE 0 END KAKAO_CNT \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('9995', '995') THEN SUM(AMOUNT) ELSE 0 END CASHA  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('9995', '995') THEN SUM(AMOUNT) ELSE 0 END CASHC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('9995', '995') THEN COUNT(1) ELSE 0 END CASH_CNT \r\n");
			strbuf.append(",CASE WHEN APPGB='A' AND ACQ_CD IN ('9994', '994') THEN SUM(AMOUNT) ELSE 0 END CASH_IC_A  \r\n");
			strbuf.append(",CASE WHEN APPGB='C' AND ACQ_CD IN ('9994', '994') THEN SUM(AMOUNT) ELSE 0 END CASH_IC_C  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('9994', '994') THEN COUNT(1) ELSE 0 END CASH_IC_CNT \r\n");
			strbuf.append("FROM   \r\n");
			strbuf.append("GLOB_MNG_ICVAN  \r\n");
			strbuf.append("WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where org_cd='OR026')  AND APPDD>='20230222' AND APPDD<='20230222'  \r\n");
			strbuf.append("GROUP BY   \r\n");
			strbuf.append("APPGB, APPDD, ADD_CASHER, ACQ_CD  \r\n");
			strbuf.append(")  \r\n");
			strbuf.append("GROUP BY   \r\n");
			strbuf.append("ADD_CASHER, APPDD  \r\n");
			strbuf.append("ORDER BY   \r\n");
			strbuf.append("ADD_CASHER ASC, APPDD ASC  \r\n");


			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0203T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			strbuf.append("ACNT, CCNT, AAMT, CAMT, ACNT+CCNT TOTCNT, AAMT-CAMT TOTAMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("SUM(ACNT) ACNT, SUM(CCNT) CCNT, SUM(AAMT) AAMT, SUM(CAMT) CAMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("CASE WHEN APPGB='A' THEN COUNT(1) ELSE 0 END ACNT, \r\n");
			strbuf.append("CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT, \r\n");
			strbuf.append("CASE WHEN APPGB='C' THEN COUNT(1) ELSE 0 END CCNT, \r\n");
			strbuf.append("CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("GLOB_MNG_ICVAN \r\n");
			strbuf.append("WHERE SVCGB IN ('CC', 'CE') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where org_cd='OR026') AND APPDD>='20230222' AND APPDD<='20230222' \r\n");
			strbuf.append("GROUP BY APPGB \r\n");
			strbuf.append(") \r\n");
			strbuf.append(")T1 \r\n");

			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0204(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			strbuf.append("DEP_NM, TID, TERM_NM, PUR_NM TR_ACQNM, MID TR_MID, ACQ_CD, ACNT, CCNT, AAMT, CAMT, ACNT+CCNT TOTCNT, AAMT-CAMT TOTAMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("TID, MID, ACQ_CD, SUM(ACNT) ACNT, SUM(CCNT) CCNT, SUM(AAMT) AAMT, SUM(CAMT) CAMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("TID, \r\n");
			strbuf.append("MID, \r\n");
			strbuf.append("ACQ_CD, \r\n");
			strbuf.append("CASE WHEN APPGB='A' THEN COUNT(1) ELSE 0 END ACNT, \r\n");
			strbuf.append("CASE WHEN APPGB='A' THEN SUM(AMOUNT) ELSE 0 END AAMT, \r\n");
			strbuf.append("CASE WHEN APPGB='C' THEN COUNT(1) ELSE 0 END CCNT, \r\n");
			strbuf.append("CASE WHEN APPGB='C' THEN SUM(AMOUNT) ELSE 0 END CAMT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("GLOB_MNG_ICVAN \r\n");
			strbuf.append("WHERE SVCGB IN ('CC', 'CE') AND AUTHCD IN ('0000', '6666') AND TID IN (select tid from tb_bas_tidmap where org_cd='OR0015') AND APPDD>='20230223' AND APPDD<='20230223' \r\n");
			strbuf.append("GROUP BY TID, MID, ACQ_CD, APPGB \r\n");
			strbuf.append(") \r\n");
			strbuf.append("GROUP BY TID, MID, ACQ_CD \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT PUR_NM, PUR_KOCES, PUR_OCD, PUR_SORT FROM TB_BAS_PURINFO )T2 ON(T1.ACQ_CD=T2.PUR_OCD OR T1.ACQ_CD=T2.PUR_KOCES ) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='OR0015' \r\n");
			strbuf.append(")T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='OR0015' \r\n");
			strbuf.append(")T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("ORDER BY TID, PUR_NM ASC \r\n");

			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0205(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			strbuf.append("    T3.DEP_CD, DEP_NM, TID TR_TID, TERM_NM TR_TIDNM, SUM(ACNT) ACNT, SUM(CCNT) CCNT, SUM(AAMT) AAMT, SUM(CAMT) CAMT, ACNT+CCNT TOTNCT, AAMT-CAMT TOTAMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("    SELECT \r\n");
			strbuf.append("TID, \r\n");
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
			strbuf.append("        $UserExpAuth[5] \r\n");
			strbuf.append("$SET_WHERE \r\n");
			strbuf.append("    GROUP BY TID, APPGB \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT TERM_ID, TERM_NM, DEP_CD FROM TB_BAS_TIDMST $ORG_WH \r\n");
			strbuf.append(")T3 ON(T3.TERM_ID=T1.TID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("    SELECT DEP_CD, DEP_NM FROM TB_BAS_DEPART $ORG_WH \r\n");
			strbuf.append(")T2 ON(T3.DEP_CD=T2.DEP_CD) \r\n");
			strbuf.append("GROUP BY T3.DEP_CD, DEP_NM, TID, TERM_NM \r\n");
			strbuf.append("ORDER BY T3.DEP_CD, TERM_NM \r\n");
			strbuf.append("); \r\n");

			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0206(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			strbuf.append("SEQNO, \r\n");
			strbuf.append("APPGB, \r\n");
			strbuf.append("TSTAT, \r\n");
			strbuf.append("DEP_NM TR_DEPNM, \r\n");
			strbuf.append("TERM_NM TR_TIDNM, \r\n");
			strbuf.append("TID TR_TID, \r\n");
			strbuf.append("MID TR_MID, \r\n");
			strbuf.append("PUR_NM TR_ACQNM, \r\n");
			strbuf.append("TSTAT_TXT TR_AUTHSTAT, \r\n");
			strbuf.append("APPDD TR_APPDD, \r\n");
			strbuf.append("APPTM TR_APPTM, \r\n");
			strbuf.append("TSTAT TR_CANDD, \r\n");
			strbuf.append("OAPPDD TR_OAPPDD, \r\n");
			strbuf.append("APPNO TR_APPNO, \r\n");
			strbuf.append("APPGB_TXT TR_AUTHTXT, \r\n");
			strbuf.append("CARDNO TR_CASH_ID, \r\n");
			strbuf.append("AMOUNT TR_AMT, \r\n");
			strbuf.append("AUTHCD TR_RST_CD, \r\n");
			strbuf.append("TRANTYPE CASH_TP, \r\n");
			strbuf.append("CANTYPE CANTYPE, \r\n");
			strbuf.append("ADD_CID ADD_PID, \r\n");
			strbuf.append("ADD_GB ADD_PGB, \r\n");
			strbuf.append("ADD_CASHER ADD_CID, \r\n");
			strbuf.append("ADD_CD ADD_RHK, \r\n");
			strbuf.append("ADD_RECP ADD_PGB, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("WHEN LENGTH(ADD_DATE) = 6 THEN '20'||ADD_DATE \r\n");
			strbuf.append("END ADD_DATE, \r\n");
			strbuf.append("TRANIDX TR_SEQNO, \r\n");
			strbuf.append("AUTHMSG APP_MSG, \r\n");
			strbuf.append("OAPP_AMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("SEQNO, DEP_NM, TERM_NM, TID, MID, PUR_NM, TSTAT, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("--정상거래 \r\n");
			strbuf.append("WHEN APPGB='A' AND OAPP_AMT IS NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR0015' AND SCD_CD='SCD0011') \r\n");
			strbuf.append("--당일취소 \r\n");
			strbuf.append("WHEN APPGB='A' AND OAPP_AMT=APPDD THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR0015' AND SCD_CD='SCD0012') \r\n");
			strbuf.append("--당일취소 \r\n");
			strbuf.append("WHEN APPGB='C' AND APPDD=OAPPDD THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR0015' AND SCD_CD='SCD0012') \r\n");
			strbuf.append("--전일취소 \r\n");
			strbuf.append("WHEN APPGB='C' AND APPDD<>OAPPDD THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR0015' AND SCD_CD='SCD0013') \r\n");
			strbuf.append("--전일취소 \r\n");
			strbuf.append("WHEN APPGB='A' AND APPDD<>OAPP_AMT AND OAPP_AMT IS NOT NULL THEN (SELECT SCD_DIP FROM TB_BAS_SITECODE WHERE ORG_CD='OR0015' AND SCD_CD='SCD0013') \r\n");
			strbuf.append("END TSTAT_TXT, \r\n");
			strbuf.append("APPDD, APPTM, TSTAT CANDATE, OAPPDD, APPNO, APPGB, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("--승인거래 \r\n");
			strbuf.append("WHEN APPGB='A' THEN '승인' \r\n");
			strbuf.append("--취소거래 \r\n");
			strbuf.append("WHEN APPGB='C' THEN '취소' \r\n");
			strbuf.append("END APPGB_TXT, \r\n");
			strbuf.append("CARDNO, AMOUNT, \r\n");
			strbuf.append("AUTHCD, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("WHEN DDCGB='0' THEN '소득공제' \r\n");
			strbuf.append("WHEN DDCGB='1' THEN '지출증빙' \r\n");
			strbuf.append("END TRANTYPE, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("WHEN OVSEA_CARD='1' THEN '거래취소' \r\n");
			strbuf.append("WHEN OVSEA_CARD='2' THEN '오류발급' \r\n");
			strbuf.append("WHEN OVSEA_CARD='3' THEN '기타' \r\n");
			strbuf.append("END CANTYPE, \r\n");
			strbuf.append("EXT_FIELD, TRANIDX, AUTHMSG \r\n");
			strbuf.append(",CASE WHEN TLINEGB IS NOT NULL THEN (SELECT CODE_VAL FROM TB_BAS_CODE WHERE TRIM(CODE_NO)=TRIM(TLINEGB)) END TLINEGBTXT \r\n");
			strbuf.append(",CASE \r\n");
			strbuf.append("WHEN ADD_GB IN ('1', 'O') THEN '외래' \r\n");
			strbuf.append("WHEN ADD_GB IN ('2', 'E') THEN '응급' \r\n");
			strbuf.append("WHEN ADD_GB IN ('3', 'I') THEN '입원' \r\n");
			strbuf.append("WHEN ADD_GB IN ('4', 'G') THEN '종합검진' \r\n");
			strbuf.append("WHEN ADD_GB='5' THEN '일반검진' \r\n");
			strbuf.append("WHEN ADD_GB='6' THEN '장례식장' \r\n");
			strbuf.append("ELSE '' \r\n");
			strbuf.append("END ADD_GB \r\n");
			strbuf.append(", ADD_CID, ADD_CD, ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, DEPO_DD, OAPP_AMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("SEQNO, BIZNO, TID, MID, VANGB, MDATE, SVCGB, T1.TRANIDX, T1.APPGB, ENTRYMD, \r\n");
			strbuf.append("T1.APPDD, APPTM, T1.APPNO, T1.CARDNO, HALBU, CURRENCY, T1.AMOUNT, AMT_UNIT, AMT_TIP, AMT_TAX, \r\n");
			strbuf.append("ISS_CD, ISS_NM, ACQ_CD, ACQ_NM, AUTHCD, AUTHMSG, CARD_CODE, CHECK_CARD, OVSEA_CARD, TLINEGB, \r\n");
			strbuf.append("SIGNCHK, DDCGB, EXT_FIELD, OAPPNO, OAPPDD, OAPPTM, OAPP_AMT, ADD_GB, ADD_CID, ADD_CD, \r\n");
			strbuf.append("ADD_RECP, ADD_CNT, ADD_CASHER, ADD_DATE, SECTION_NO, PUR_NM, DEP_NM, TERM_NM, \r\n");
			strbuf.append("DEPOREQDD DEPO_DD, \r\n");
			strbuf.append("CASE \r\n");
			strbuf.append("WHEN APPGB='C' THEN '' \r\n");
			strbuf.append("WHEN APPGB='A' THEN (SELECT C1.APPDD FROM GLOB_MNG_ICVAN C1 WHERE C1.APPGB='C' AND T1.APPDD=C1.OAPPDD AND T1.APPNO=C1.APPNO AND T1.AMOUNT=C1.AMOUNT AND T1.CARDNO=C1.CARDNO) \r\n");
			strbuf.append("END TSTAT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("GLOB_MNG_ICVAN T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_CD, TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='OR0015' \r\n");
			strbuf.append(")T3 ON(T1.TID=T3.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='OR0015' \r\n");
			strbuf.append(")T4 ON(T3.DEP_CD=T4.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( SELECT PUR_NM, PUR_OCD, PUR_KOCES, PUR_CD FROM TB_BAS_PURINFO)T5 ON (T1.ACQ_CD=T5.PUR_OCD OR T1.ACQ_CD=T5.PUR_KOCES OR T1.ACQ_CD=T5.PUR_CD) \r\n");
			strbuf.append("WHERE SVCGB IN ('CB') AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap where org_cd='OR0015') AND T1.APPDD>='20230223' AND (T1.APPDD<='20230223') \r\n");
			strbuf.append("order by appdd desc, apptm desc \r\n");
			strbuf.append(") \r\n");
			strbuf.append(") \r\n");

			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0206T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0207(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0207T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0208(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0209(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0210(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * sub0301 검색 Query
	 * @param jary(tb_sys_domain)
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-16 김태균
	 */
	public JSONArray get_sub0301(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			
			strbuf.append("SELECT \r\n");
			strbuf.append("DEP_NM TR_DEPNM, T3.DEP_CD DEP_CD, PUR_NM TR_ACQNM, T1.MID TR_MID, T1.EXP_DD DEP_EXP_DD, T_CNT TOT_CNT \r\n");
			strbuf.append(", T_BAN TOT_BAN, T_AMT TOT_AMT, T_FEE TOT_FEE, T_EXP TOT_EXP \r\n");
			strbuf.append(", I_CNT DEP_CNT \r\n");
			strbuf.append(", I_BAN DEP_BAN \r\n");
			strbuf.append(", I_AMT DEP_AMT \r\n");
			strbuf.append(", I_FEE DEP_FEE \r\n");
			strbuf.append(", I_EXP DEP_EXP \r\n");
			strbuf.append(", BANK_AMT \r\n");
			strbuf.append(", NVL(BANK_AMT,0) \r\n");
			strbuf.append(", TO_NUMBER(T_EXP-I_EXP) AS DIF_TOT_AMT \r\n");
			strbuf.append(", TO_NUMBER(NVL(BANK_AMT,0)-I_EXP) AS DIF_BANK_AMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("MID, EXP_DD, SUM(TOT_CNT) T_CNT, SUM(TOT_BAN) T_BAN, SUM(TOT_NETAMT) T_AMT \r\n");
			strbuf.append(", SUM(TOT_INPAMT) T_FEE, SUM(TOT_EXPAMT) T_EXP, SUM(I_CNT) I_CNT, SUM(I_BAN) I_BAN \r\n");
			strbuf.append(", SUM(I_AMT) I_AMT, SUM(I_FEE) I_FEE, SUM(I_EXP) I_EXP \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("MID, EXP_DD, DEP_SEQ,SUM(TOT_CNT) TOT_CNT ,SUM(BAN_CNT) TOT_BAN,(SUM(EXP_AMT)+SUM(INP_AMT)) TOT_NETAMT \r\n");
			strbuf.append(",SUM(INP_AMT) TOT_INPAMT, SUM(EXP_AMT) TOT_EXPAMT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_MNG_DEPTOT \r\n");
			strbuf.append("WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where org_cd='OR026') AND EXP_DD>='20230222' AND EXP_DD<='20230222' \r\n");
			strbuf.append("GROUP BY MID, EXP_DD, DEP_SEQ \r\n");
			strbuf.append("ORDER BY EXP_DD DESC \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("DEP_SEQ \r\n");
			strbuf.append(", (SUM(ITEM_CNT60)+SUM(ITEM_CNT67)) I_CNT \r\n");
			strbuf.append(", SUM(ITEM_CNTBAN) I_BAN \r\n");
			strbuf.append(", (SUM(ITEM_AMT60)-SUM(ITEM_AMT67)) I_AMT \r\n");
			strbuf.append(", (SUM(ITEM_FEE60)-SUM(ITEM_FEE67)) I_FEE \r\n");
			strbuf.append(", (SUM(ITEM_AMT60)-SUM(ITEM_AMT67))-(SUM(ITEM_FEE60)-SUM(ITEM_FEE67)) I_EXP \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("DEP_SEQ \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN COUNT(1) ELSE 0 END ITEM_CNT60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN COUNT(1) ELSE 0 END ITEM_CNT67 \r\n");
			strbuf.append(",CASE WHEN RTN_CD NOT IN ('60', '67') THEN COUNT(1) ELSE 0 END ITEM_CNTBAN \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT67 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN SUM(FEE) ELSE 0 END ITEM_FEE60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN SUM(FEE) ELSE 0 END ITEM_FEE67 \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_MNG_DEPDATA \r\n");
			strbuf.append("WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where org_cd='OR026') AND EXP_DD>='20230222' AND EXP_DD<='20230222' \r\n");
			strbuf.append("GROUP BY DEP_SEQ, RTN_CD \r\n");
			strbuf.append(") \r\n");
			strbuf.append("GROUP BY DEP_SEQ \r\n");
			strbuf.append(")T2 ON(T1.DEP_SEQ=T2.DEP_SEQ) \r\n");
			strbuf.append("GROUP BY MID, EXP_DD \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("EXP_DD \r\n");
			strbuf.append(", MID \r\n");
			strbuf.append(", CASE WHEN SUM(EXP_AMT) IS NULL THEN 0 ELSE SUM(EXP_AMT) END BANK_AMT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_MNG_BANKDATA \r\n");
			strbuf.append("GROUP BY EXP_DD, MID \r\n");
			strbuf.append(")T2 ON(T1.MID=T2.MID AND T1.EXP_DD=T2.EXP_DD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT ORG_CD, DEP_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO \r\n");
			strbuf.append(")T3 ON(T1.MID=T3.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT ORG_CD, ORG_NM FROM TB_BAS_ORG \r\n");
			strbuf.append(")T4 ON(T3.ORG_CD=T4.ORG_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_CD, DEP_NM FROM TB_BAS_DEPART \r\n");
			strbuf.append(")T5 ON(T3.DEP_CD=T5.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT PUR_CD, PUR_NM, PUR_SORT, PUR_KOCES,PUR_OCD FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T6 ON(T3.PUR_CD=T6.PUR_CD) \r\n");
			strbuf.append("ORDER BY DEP_NM ASC, PUR_NM ASC, PUR_SORT ASC \r\n");

			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0302(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			strbuf.append("CONCAT(T1.DEP_SEQ, T1.DEP_CD) SEQNO, \r\n");
			strbuf.append("T1.DEP_CD, CARD_NO, EXP_DD, MID, REQ_DD, TID, RTN_CD, T1.APP_DD, \r\n");
			strbuf.append("REG_DD, HALBU, SALE_AMT, RSC_CD, RS_MSG, T1.APP_NO, T2.DEP_CD DPCD, T2.STO_CD, \r\n");
			strbuf.append("FEE, DEP_NM, STO_NM, PUR_NM, TERM_NM, EXT_FIELD,ADD_CID,ADD_CASHER \r\n");
			strbuf.append(", CASE \r\n");
			strbuf.append("WHEN LENGTH(ADD_DATE) = 6 THEN '20'||ADD_DATE \r\n");
			strbuf.append("END ADD_DATE \r\n");
			strbuf.append(",TRUNC((FEE/SALE_AMT*100),2)||'%' AS FEEPER \r\n");
			strbuf.append(",APPTM \r\n");
			strbuf.append(",OAPPDD \r\n");
			strbuf.append(",(SALE_AMT-FEE) EXP_AMT\r\n");
			strbuf.append(",'신용' TR_CARDTP\r\n");
			strbuf.append(",'국내' OVERSEA_CARD\r\n");
			strbuf.append(",CASE WHEN RTN_CD in ('60','67') THEN '정상매입' ELSE '매출반송' END RTN_MSG \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT ORG_CD, DEP_CD, STO_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO WHERE ORG_CD='OR026' \r\n");
			strbuf.append(")T2 ON(T1.MID=T2.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD='OR026' \r\n");
			strbuf.append(")T6 ON(T1.TID=T6.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART WHERE ORG_CD='OR026' \r\n");
			strbuf.append(")T3 ON(T2.DEP_CD=T3.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT STO_NM, STO_CD, DEP_CD, ORG_CD FROM TB_BAS_STORE \r\n");
			strbuf.append(")T4 ON(T2.STO_CD=T4.STO_CD AND T2.DEP_CD=T4.DEP_CD AND T2.ORG_CD=T4.ORG_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT PUR_CD, PUR_NM FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T6 ON(T2.PUR_CD=T6.PUR_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT APPDD, TRANIDX, EXT_FIELD,ADD_CID, ADD_CASHER,ADD_DATE,APPTM,OAPPDD FROM GLOB_MNG_ICVAN \r\n");
			strbuf.append(")T7 ON(T1.APP_DD=T7.APPDD AND T1.TRANIDX=T7.TRANIDX) \r\n");
			strbuf.append("WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where org_cd='OR026') AND EXP_DD>='20230222' AND EXP_DD<='20230222' \r\n");

			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0302T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			strbuf.append("MID, DEP_NM TR_DEPNM, PUR_NM TR_ACQNM, PUR_SORT  \r\n");
			strbuf.append(", (SUM(ITEM_CNT60)+SUM(ITEM_CNT67)) I_CNTTOT_CNT \r\n");
			strbuf.append(", SUM(ITEM_CNTBAN) I_BAN_TOT_BAN \r\n");
			strbuf.append(", (SUM(ITEM_AMT60)-SUM(ITEM_AMT67)) I_AMTTOT_AMT \r\n");
			strbuf.append(", (SUM(ITEM_FEE60)-SUM(ITEM_FEE67)) I_FEETOT_FEE \r\n");
			strbuf.append(", (SUM(ITEM_AMT60)-SUM(ITEM_AMT67))-(SUM(ITEM_FEE60)-SUM(ITEM_FEE67)) I_EXP_TOT_EXP \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("MID, DEP_NM, PUR_NM, PUR_SORT \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN COUNT(1) ELSE 0 END ITEM_CNT60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN COUNT(1) ELSE 0 END ITEM_CNT67 \r\n");
			strbuf.append(",CASE WHEN RTN_CD NOT IN ('60', '67') THEN COUNT(1) ELSE 0 END ITEM_CNTBAN \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT67 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN SUM(FEE) ELSE 0 END ITEM_FEE60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN SUM(FEE) ELSE 0 END ITEM_FEE67 \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("T1.DEP_CD, CARD_NO, EXP_DD, MID, REQ_DD, TID, RTN_CD, \r\n");
			strbuf.append("REG_DD, HALBU, SALE_AMT, RSC_CD, RS_MSG, \r\n");
			strbuf.append("FEE, DEP_NM, PUR_NM, T6.STO_CD PUR_SORT \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_MNG_DEPDATA T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT ORG_CD, DEP_CD, STO_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO where org_cd='OR026' \r\n");
			strbuf.append(")T2 ON(T1.MID=T2.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST where org_cd='OR026' \r\n");
			strbuf.append(")T6 ON(T1.TID=T6.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_NM, DEP_CD FROM TB_BAS_DEPART where org_cd='OR026' \r\n");
			strbuf.append(")T3 ON(T2.DEP_CD=T3.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT PUR_CD, PUR_NM, STO_CD FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T6 ON(T2.PUR_CD=T6.PUR_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT APPDD, TRANIDX, EXT_FIELD,ADD_CID, CHECK_CARD FROM GLOB_MNG_ICVAN \r\n");
			strbuf.append(")T7 ON(T1.APP_DD=T7.APPDD AND T1.TRANIDX=T7.TRANIDX) \r\n");
			strbuf.append("WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where org_cd='OR026') AND EXP_DD>='20230222' AND EXP_DD<='20230222' \r\n");
			strbuf.append(") \r\n");
			strbuf.append("GROUP BY MID, DEP_NM, PUR_NM, PUR_SORT, RTN_CD \r\n");
			strbuf.append(") \r\n");
			strbuf.append("GROUP BY MID, DEP_NM, PUR_NM, PUR_SORT \r\n");
			strbuf.append("ORDER BY PUR_SORT ASC \r\n");

			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0303(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			strbuf.append("T5.DEP_NM TR_DEPNM \r\n");
			strbuf.append(", T2.TERM_NM TR_TIDNM \r\n");
			strbuf.append(", T6.PUR_NM TR_ACQNM \r\n");
			strbuf.append(", APP_DD TR_APPDD \r\n");
			strbuf.append(", REQ_DD \r\n");
			strbuf.append(", EXP_DD TR_EXPDD \r\n");
			strbuf.append(", TID TR_TID \r\n");
			strbuf.append(", MID TR_MID \r\n");
			strbuf.append(", ITEM_CNT \r\n");
			strbuf.append(", ITEM_AMT \r\n");
			strbuf.append(", ITEM_FEE \r\n");
			strbuf.append(", ITEM_EXP \r\n");
			strbuf.append(", '0' CK_CNT \r\n");
			strbuf.append(", (ITEM_CNT+0) TOT_CNT \r\n");
			strbuf.append(", '0' CK_AMT \r\n");
			strbuf.append(", (ITEM_AMT+0) TOT_AMT \r\n");
			strbuf.append(", '0' CK_FEE \r\n");
			strbuf.append(", (ITEM_FEE+0) TOT_FEE \r\n");
			strbuf.append(", (ITEM_AMT-ITEM_FEE) ITEM_SUM \r\n");
			strbuf.append(", (0-0) CK_SUM \r\n");
			strbuf.append(", (ITEM_AMT) SUM_AMT \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("APP_DD \r\n");
			strbuf.append(", REQ_DD \r\n");
			strbuf.append(", EXP_DD \r\n");
			strbuf.append(", TID \r\n");
			strbuf.append(", MID \r\n");
			strbuf.append(", SUM(ITEM_CNT60)+SUM(ITEM_CNT67) ITEM_CNT \r\n");
			strbuf.append(", SUM(ITEM_AMT60)-SUM(ITEM_AMT67) ITEM_AMT \r\n");
			strbuf.append(", SUM(ITEM_FEE60)-SUM(ITEM_FEE67) ITEM_FEE \r\n");
			strbuf.append(", (SUM(ITEM_AMT60)-SUM(ITEM_AMT67))-(SUM(ITEM_FEE60)-SUM(ITEM_FEE67)) ITEM_EXP \r\n");
			strbuf.append("FROM( \r\n");
			strbuf.append("SELECT \r\n");
			strbuf.append("APP_DD \r\n");
			strbuf.append(",REQ_DD \r\n");
			strbuf.append(",EXP_DD \r\n");
			strbuf.append(",TID \r\n");
			strbuf.append(",MID \r\n");
			strbuf.append(",RTN_CD \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN COUNT(1) ELSE 0 END ITEM_CNT60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN COUNT(1) ELSE 0 END ITEM_CNT67 \r\n");
			strbuf.append(",CASE WHEN RTN_CD NOT IN ('60', '67') THEN COUNT(1) ELSE 0 END ITEM_CNTBAN \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END ITEM_AMT67 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='60' THEN SUM(FEE) ELSE 0 END ITEM_FEE60 \r\n");
			strbuf.append(",CASE WHEN RTN_CD='67' THEN SUM(FEE) ELSE 0 END ITEM_FEE67 \r\n");
			strbuf.append("FROM \r\n");
			strbuf.append("TB_MNG_DEPDATA \r\n");
			strbuf.append("WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where org_cd='OR026') AND APP_DD>='20230222' AND APP_DD<='20230222' \r\n");
			strbuf.append("GROUP BY APP_DD, REQ_DD, EXP_DD, MID, TID, RTN_CD \r\n");
			strbuf.append(") \r\n");
			strbuf.append("GROUP BY APP_DD, REQ_DD, EXP_DD, TID, MID \r\n");
			strbuf.append(")T1 \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT TERM_ID, TERM_NM FROM TB_BAS_TIDMST WHERE ORG_CD='OR026' \r\n");
			strbuf.append(")T2 ON(T1.TID=T2.TERM_ID) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT ORG_CD, DEP_CD, MER_NO, PUR_CD FROM TB_BAS_MERINFO WHERE ORG_CD='OR026' \r\n");
			strbuf.append(")T3 ON(T1.MID=T3.MER_NO) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT ORG_CD, ORG_NM FROM TB_BAS_ORG \r\n");
			strbuf.append(")T4 ON(T3.ORG_CD=T4.ORG_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT DEP_CD, DEP_NM FROM TB_BAS_DEPART WHERE ORG_CD='OR026' \r\n");
			strbuf.append(")T5 ON(T3.DEP_CD=T5.DEP_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT PUR_CD, PUR_NM, PUR_SORT, PUR_KOCES,PUR_OCD FROM TB_BAS_PURINFO \r\n");
			strbuf.append(")T6 ON(T3.PUR_CD=T6.PUR_CD) \r\n");
			strbuf.append("LEFT OUTER JOIN( \r\n");
			strbuf.append("SELECT ORG_CD, USER_PUR_CD, USER_PURSORT FROM TB_BAS_USERPURINFO WHERE ORG_CD='OR026' \r\n");
			strbuf.append(")S3 ON(T6.PUR_CD=S3.USER_PUR_CD) \r\n");
			strbuf.append("WHERE ITEM_CNT>0 \r\n");
			strbuf.append("ORDER BY T3.DEP_CD ASC, T1.APP_DD ASC, T6.PUR_NM ASC, T1.REQ_DD ASC, T1.EXP_DD ASC \r\n");

			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0303T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT  \r\n");
			strbuf.append("T2.TERM_NM TR_TIDNM  \r\n");
			strbuf.append(",TID TR_TID  \r\n");
			strbuf.append(",BC  \r\n");
			strbuf.append(",NH  \r\n");
			strbuf.append(",KB  \r\n");
			strbuf.append(",SS  \r\n");
			strbuf.append(",HN  \r\n");
			strbuf.append(",LO  \r\n");
			strbuf.append(",HD  \r\n");
			strbuf.append(",SI  \r\n");
			strbuf.append(",BCF  \r\n");
			strbuf.append(",NHF  \r\n");
			strbuf.append(",KBF  \r\n");
			strbuf.append(",SSF  \r\n");
			strbuf.append(",HNF  \r\n");
			strbuf.append(",LOF  \r\n");
			strbuf.append(",HDF  \r\n");
			strbuf.append(",SIF  \r\n");
			strbuf.append(",JH \r\n");
			strbuf.append(",AP \r\n");
			strbuf.append(",WP \r\n");
			strbuf.append(",ZERO \r\n");
			strbuf.append(",KAKAO \r\n");
			strbuf.append(",JHF \r\n");
			strbuf.append(",APF \r\n");
			strbuf.append(",WPF \r\n");
			strbuf.append(",ZEROF \r\n");
			strbuf.append(",KAKAOF \r\n");
			strbuf.append("FROM(  \r\n");
			strbuf.append("SELECT  \r\n");
			strbuf.append("TID  \r\n");
			strbuf.append(",SUM(BCA)-SUM(BCC) BC  \r\n");
			strbuf.append(",SUM(NHA)-SUM(NHC) NH  \r\n");
			strbuf.append(",SUM(KBA)-SUM(KBC) KB  \r\n");
			strbuf.append(",SUM(SSA)-SUM(SSC) SS  \r\n");
			strbuf.append(",SUM(HNA)-SUM(HNC) HN  \r\n");
			strbuf.append(",SUM(LOA)-SUM(LOC) LO  \r\n");
			strbuf.append(",SUM(HDA)-SUM(HDC) HD  \r\n");
			strbuf.append(",SUM(SIA)-SUM(SIC) SI  \r\n");
			strbuf.append(",SUM(BCAF)-SUM(BCCF) BCF  \r\n");
			strbuf.append(",SUM(NHAF)-SUM(NHCF) NHF  \r\n");
			strbuf.append(",SUM(KBAF)-SUM(KBCF) KBF  \r\n");
			strbuf.append(",SUM(SSAF)-SUM(SSCF) SSF  \r\n");
			strbuf.append(",SUM(HNAF)-SUM(HNCF) HNF  \r\n");
			strbuf.append(",SUM(LOAF)-SUM(LOCF) LOF  \r\n");
			strbuf.append(",SUM(HDAF)-SUM(HDCF) HDF  \r\n");
			strbuf.append(",SUM(SIAF)-SUM(SICF) SIF  \r\n");
			strbuf.append(",SUM(JHA)-SUM(JHC) JH \r\n");
			strbuf.append(",SUM(APA)-SUM(APC) AP \r\n");
			strbuf.append(",SUM(WPA)-SUM(WPC) WP \r\n");
			strbuf.append(",SUM(ZEROA)-SUM(ZEROC) ZERO \r\n");
			strbuf.append(",SUM(KAKAOA)-SUM(KAKAOC) KAKAO \r\n");
			strbuf.append(",SUM(JHAF)-SUM(JHCF) JHF  \r\n");
			strbuf.append(",SUM(APAF)-SUM(APCF) APF \r\n");
			strbuf.append(",SUM(WPAF)-SUM(WPCF) WPF  \r\n");
			strbuf.append(",SUM(ZEROAF)-SUM(ZEROCF) ZEROF  \r\n");
			strbuf.append(",SUM(KAKAOAF)-SUM(KAKAOCF) KAKAOF  \r\n");
			strbuf.append("FROM(  \r\n");
			strbuf.append("SELECT  \r\n");
			strbuf.append("TID  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END BCA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END NHA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END KBA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END SSA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END HNA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END LOA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END HDA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END SIA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END BCC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END NHC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END KBC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END SSC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END HNC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END LOC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END HDC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END SIC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END BCAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END NHAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END KBAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END SSAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END HNAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END LOAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END HDAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END SIAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0006', '026', '1106','01') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END BCCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0030', '018', '2211','11') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END NHCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0001', '016', '1101','02') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END KBCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0004', '031', '1104','06') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END SSCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0005', '008', '1105','03') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END HNCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0003', '047', '1103','33') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END LOCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0002', '027', '1102','08') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END HDCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC0007', '029', '1107','07') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END SICF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END JHA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END JHC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END JHAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9999', '999', '9999','99') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END JHCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END APA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END APC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END APAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9998', '998', '9998','98') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END APCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END WPA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END WPC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END WPAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9997', '997', '9997','97') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END WPCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END ZEROA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END ZEROC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END ZEROAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9996', '996', '9996','96') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END ZEROCF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='60' THEN SUM(SALE_AMT) ELSE 0 END KAKAOA  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='67' THEN SUM(SALE_AMT) ELSE 0 END KAKAOC  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='60' THEN SUM(FEE) ELSE 0 END KAKAOAF  \r\n");
			strbuf.append(",CASE WHEN ACQ_CD IN ('VC9995', '995', '9995','95') AND RTN_CD='67' THEN SUM(FEE) ELSE 0 END KAKAOCF  \r\n");
			strbuf.append("FROM  \r\n");
			strbuf.append("TB_MNG_DEPDATA  \r\n");
			strbuf.append("WHERE MID IN (SELECT MID FROM TB_BAS_MIDMAP where org_cd='OR026') AND APP_DD>='20230222' AND APP_DD<='20230222'  \r\n");
			strbuf.append("GROUP BY  \r\n");
			strbuf.append("TID, ACQ_CD, RTN_CD  \r\n");
			strbuf.append(") GROUP BY TID  \r\n");
			strbuf.append(") T1  \r\n");
			strbuf.append("LEFT OUTER JOIN(  \r\n");
			strbuf.append("SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST  \r\n");
			strbuf.append(")T2 ON(T1.TID=T2.TERM_ID)  \r\n");


			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0304(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0304T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0305(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0305T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0306(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0307(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0307T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0308(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0308T(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-22 장현석
	 */
	public JSONArray get_sub0309(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			//쿼리입력
			strbuf.append("SELECT \r\n");
			
			//System.lineSeparator()
			
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			
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
					System.out.println("성공");
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
	 * depcd 컬럼 Select Query
	 * @param orgcd
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-01 김태균
	 */
	public JSONArray get_depcd(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT DEP_CD,DEP_NM FROM TB_BAS_DEPART WHERE org_cd = ?");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, orgcd); //유저 ID
			
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
	 * acqpcd 컬럼 Select Query
	 * @return json형식
	 * 2023-02-13 장현석
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
			System.out.println(strbuf.toString());	//로그
			
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
	 * tidcd 컬럼 Select Query
	 * @param orgcd
	 * @return json
	 * 2023-02-13 장현석
	 */
	public JSONArray get_tidcd(String orgcd) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("SELECT TERM_NM, TERM_ID FROM TB_BAS_TIDMST WHERE ORG_CD = ?");
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, orgcd); //유저 ID
			
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
	 * 메뉴 컬럼 Select Query
	 * @param auth_seq,orgcd
	 * @return json
	 * 2023-02-13 김태균
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
			strbuf.append(" WHERE A.AUTH_SEQ=? AND ORGCD=? ");
			strbuf.append(" ORDER BY B.SORT ASC ");
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, auth_seq); //유저 ID
			stmt.setString(2, orgcd); //ORGCD
				
			
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
	 * 즐겨찾기 메뉴 컬럼 Select Query
	 * @param user_id
	 * @return json
	 * 2023-02-13 김태균
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
			strbuf.append("WHERE T1.USER_ID = ? ORDER BY SORT ASC");
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, userid); //유저 ID
				
			
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
	 * 즐겨찾기 메뉴 컬럼 INSERT Query
	 * @param user_id,program_seq,sort
	 * @return 1 : 성공 / 0 : 실패 (인서트된 로우 수)
	 * 2023-02-13 김태균
	 */
	public int insert_favorite(String userid,String program_seq,String sort) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("INSERT INTO TB_SYS_FAVORITE (USER_ID,PROGRAM_SEQ,SORT,USE_YN) VALUES(?,?,?,'Y')");
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, userid); //유저 ID
			stmt.setString(2, program_seq); //PROGRAM_SEQ SEQ
			stmt.setString(3, sort); //SORT(순번)
			
			
				
			
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
	 * 즐겨찾기 메뉴 컬럼 DELETE Query
	 * @param user_id,program_seq,sort
	 * @return 1 : 성공 / 0 : 실패 (삭제된 로우 수)
	 * 2023-02-13 김태균
	 */
	public int del_favorite(String userid,String program_seq,String sort) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("DELETE FROM TB_SYS_FAVORITE WHERE ");
			strbuf.append("USER_ID = ? AND ");
			strbuf.append("PROGRAM_SEQ = ? AND ");
			strbuf.append("SORT = ?");
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, userid); //유저 ID
			stmt.setString(2, program_seq); //PROGRAM_SEQ SEQ
			stmt.setString(3, sort); //SORT(순번)
			
			
				
			
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
	 * 즐겨찾기 메뉴 컬럼 UPDATE Query
	 * @param user_id,program_seq,sort
	 * @return 1 : 성공 / 0 : 실패 (삭제된 로우 수)
	 * 2023-02-13 김태균
	 */
	public int mod_favorite(String userid,String program_seq,String sort,String new_sort) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			strbuf = new StringBuffer();
			strbuf.append("UPDATE TB_SYS_FAVORITE SET SORT = ? WHERE ");
			strbuf.append("USER_ID = ? AND ");
			strbuf.append("PROGRAM_SEQ = ? AND ");
			strbuf.append("SORT = ? ");
					
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, new_sort); //NEW_SORT(순번)
			stmt.setString(2, userid); //유저 ID
			stmt.setString(3, program_seq); //PROGRAM_SEQ SEQ
			stmt.setString(4, sort); //SORT(순번)
			
			
				
			
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
