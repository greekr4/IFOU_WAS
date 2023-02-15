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
	            jsonob.put("width",rs.getInt("WIDTHS"));
	            jsonob.put("id",rs.getString("POS_FIELD"));
	            jsonob.put("header",rs.getString("FIELDS_TXT"));
	            
	            if(rs.getString("SORTS").equals("int")) {
	            jsonob.put("format","#,###");
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
	 * glob_mng_icvan Select Query
	 * @param jary(tb_sys_domain)
	 * @return json형식으로 dhx형식에 맞게
	 * 2023-02-01 김태균
	 */
	public JSONArray select_glob_mng_icvan(JSONArray jary) {
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
			strbuf.append(" OAPP_AMT    TR_CANDD\r\n");
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
			strbuf.append("		WHERE SVCGB IN ('CC', 'CE')  AND AUTHCD='0000' AND TID IN (select tid from tb_bas_tidmap  where org_cd='OR008' AND dep_cd='DP30101')   AND T1.APPDD>='20220203' and (T1.APPDD<='20230203' AND T1.APPTM<='155357')\r\n");
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
					jsonob.put(id,rs.getString(id));
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
	public JSONArray select_glob_mng_icvan_tot(JSONArray jary) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JSONArray jsonary = new JSONArray();
		
		try {
			strbuf = new StringBuffer();
			
			strbuf.append("SELECT\r\n"
					+ "     DEP_NM\r\n"
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
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
				for(int i=0; i<jary.size();i++) {
					JSONObject jsonob2 = new JSONObject();
					jsonob2 = (JSONObject) jary.get(i);	
					String id = (String)(jsonob2.get("id"));
					System.out.println(id+"ck");
					if(!id.equals("ORN")) {
					System.out.println(id + " = " + (rsmd.getCatalogName(1)));
					jsonob.put(id,rs.getString(id));
					System.out.println("성공");
					}else {
					jsonob.put("ORN", i);
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
			strbuf.append("SELECT\r\n"
					+ "	A.PROGRAM_SEQ MENU_SEQ,\r\n"
					+ "	B.PROGRAM_NAME MENU_NAME,\r\n"
					+ "	B.DEPTH MENU_DEPTH,\r\n"
					+ "	B.PARENT_SEQ PARENT_SEQ,\r\n"
					+ "	A.ENABLE_READ AUTH_R,\r\n"
					+ "	A.ENABLE_CREATE AUTH_C,\r\n"
					+ "	A.ENABLE_UPDATE AUTH_U,\r\n"
					+ "	A.ENABLE_DELETE AUTH_D,\r\n"
					+ "	B.SRC_LOCATION MURL\r\n"
					+ "FROM \r\n"
					+ "    TB_SYS_MENU A\r\n"
					+ "LEFT OUTER JOIN\r\n"
					+ "    (SELECT PROGRAM_SEQ, PROGRAM_NAME, PARENT_SEQ, DEPTH, SRC_LOCATION, SORT FROM TB_SYS_PROGRAM) B\r\n"
					+ "ON (A.PROGRAM_SEQ=B.PROGRAM_SEQ)\r\n"
					+ "WHERE A.AUTH_SEQ=? AND ORGCD=? \r\n"
					+ "ORDER BY B.SORT ASC");
			
			con = getOraConnect();
			stmt = con.prepareStatement(strbuf.toString());
			System.out.println(strbuf.toString());	//로그
			stmt.setString(1, auth_seq); //유저 ID
			stmt.setString(2, orgcd); //유저 ID
				
			
			rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject jsonob = new JSONObject();
	            jsonob.put("MENU_SEQ",rs.getString("MENU_SEQ"));
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
	
	
	
	
	
}
