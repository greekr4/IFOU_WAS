package com.gaon.nifou.v3;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class trans_util_manager {
	
	private java.io.File folder = null;
	
	private static SimpleDateFormat NowDate = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat NowDateShot = new SimpleDateFormat("yyMMdd");
	private static SimpleDateFormat NowTime = new SimpleDateFormat("HHmmss");
	private static SimpleDateFormat FullDate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	private static SimpleDateFormat FullDate2 = new SimpleDateFormat("yyyyMMddHHmmss");
	private static SimpleDateFormat NowMinu = new SimpleDateFormat("mm");
	private static SimpleDateFormat NowSecon = new SimpleDateFormat("ss");
	private static SimpleDateFormat y4 = new SimpleDateFormat("yyyy");
	private static SimpleDateFormat m2 = new SimpleDateFormat("MM");
	private static SimpleDateFormat d2 = new SimpleDateFormat("dd");
	
	public String DateFormatFull(){
		Date toDay = new Date();	
		String nowdate	= FullDate.format(toDay);
		return nowdate;
	}
	
	public String DateFormatFull2(){
		Date toDay = new Date();	
		String nowdate	= FullDate2.format(toDay);
		return nowdate;
	}
	
	public String DateY4(){
		Date toDay = new Date();	
		String nowdate	= y4.format(toDay);
		return nowdate;
	}
	
	public String DateM2(){
		Date toDay = new Date();	
		String nowdate	= m2.format(toDay);
		return nowdate;
	}
	
	public String DateD2(){
		Date toDay = new Date();	
		String nowdate	= d2.format(toDay);
		return nowdate;
	}
	
	public String DateFormatNowDate(){
		Date toDay = new Date();	
		String nowdate	= NowDate.format(toDay);
		return nowdate;
	}
	
	public String NowDateShot(){
		Date toDay = new Date();	
		String nowdate	= NowDateShot.format(toDay);
		return nowdate;
	}
	
	public String DateFormatNowTime(){
		Date toDay = new Date();	
		String nowdate	= NowTime.format(toDay);
		return nowdate;
	}
	
	public String DateFormatYesDay(int d){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, d);
		Date today = cal.getTime();
		SimpleDateFormat fomatter = new SimpleDateFormat("yyyyMMdd");
		
		return fomatter.format(today);
	}
	
	public String YesDateShot(int d){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, d);
		Date today = cal.getTime();
		SimpleDateFormat fomatter = new SimpleDateFormat("yyMMdd");
		
		return fomatter.format(today);
	}
	
	public String DateFormatNowMinu(){
		Date toDay = new Date();	
		String rtn	= NowMinu.format(toDay);
		
		return rtn;
	}
	
	public String DateFormatNowSecon(){
		Date toDay = new Date();	
		String rtn	= NowSecon.format(toDay);
		
		return rtn;
	}
	
	public String WeekCount(){
		String[] DaytoDay = {"일","월","화","수","목","금","토"};
		Calendar cal = Calendar.getInstance();
		int no = cal.get(Calendar.DAY_OF_WEEK) - 1 -1;
		if( no < 0 ){
			no=6;
		}
		return DaytoDay[no];
	}
	
	public void MakeFolder(File tfolder) {	
		folder = tfolder;
		
        if(!folder.exists()){
			try{
				folder.mkdirs();
			}catch(Exception ex){
				System.out.println(ex);
			}
		}
	}
	
	//private static final String EMPTY = null;

	/*
	* STring을 지정한 길이만큼  왼쪽으로 채움.
	* @param str 원본문자열
	* @param len 채울길이
	* @param addStr 채울문자
	* @return 변환값
	*/
	public String lpad(String str, int len, String addStr) {

         String result = str;
         int templen   = len - result.length();
 
        for (int i = 0; i < templen; i++){
               result = addStr + result;
         }
         
         return result;
	}
	
	public String rpad(String str, int len, String addStr) {

        String result = str;
        int templen   = len - result.length();

       for (int i = 0; i < templen; i++){
              result = result + addStr;
        }
        
        return result;
	}
	
	public String replace_space(String str) {

        String result 	= "";
        String tmpstr	= "";
        
        if(str.equals("Y")){
        	result	= str;
        }else{
        	result  = " "; 
        }
        
        return result;
	}
	
	public String PtbsFlag(String str) {

        String result 	= "";
        
        if(str.equals("E")){
        	result	= "E";
        }else if(str.equals("Y")){
        	result  = "Y"; 
        }else if(str.equals("N")){
        	result  = "N"; 
        }else if(str.equals("NOPTBS")){
        	result  = "B"; 
        }else if(str.equals("NOOTPT")){
        	result  = "P"; 
        }else if(str.equals("NOSPEC")){
        	result  = "C"; 
        }
        
        return result;
	}
	
	
	public static String AddBySpace(String obj, int sLen){
		byte[] SpcStr = new byte[sLen];
		int RtnStrLen	= obj.length();
		String RtnStr	= "";
		
		for(int i=obj.length();i<sLen;i++){
			RtnStr += " "; 
		}		
		
		return obj+RtnStr;
	}
	
	public String kString(String strData, int iStartPos, int iByteLength) {
		 
		byte[] bytTemp = null;
 		int iRealStart = 0;
 		int iRealEnd = 0;
 		int iLength = 0;
 		int iChar = 0;

 		try {
 		// UTF-8로 변환하는경우 한글 2Byte, 기타 1Byte로 떨어짐
			bytTemp = strData.getBytes("EUC-KR");
			iLength = bytTemp.length;

			for(int iIndex = 0; iIndex < iLength; iIndex++) {
 				if(iStartPos <= iIndex) {
 					break;
 				}
 
 				iChar = bytTemp[iIndex];
 				if((iChar > 127)|| (iChar < 0)) {
 					// 한글의 경우(2byte 통과처리)
 					// 한글은 2Byte이기 때문에 다음 글자는 볼것도 없이 스킵한다
 					iRealStart++;
 					iIndex++;
 				} else {
 					// 기타 글씨(1Byte 통과처리)
 					iRealStart++;
 				}
 			}
 			
			iRealEnd = iRealStart;
			int iEndLength = iRealStart + iByteLength;

			for(int iIndex = iRealStart; iIndex < iEndLength; iIndex++)
 			{
 				iChar = bytTemp[iIndex];
 				if((iChar > 127)|| (iChar < 0)) {
 					// 한글의 경우(2byte 통과처리)
 					// 한글은 2Byte이기 때문에 다음 글자는 볼것도 없이 스킵한다
 					iRealEnd++;
 					iIndex++;
 				} else {
 					// 기타 글씨(1Byte 통과처리)
 					iRealEnd++;
 				}
 			}
 		} catch(Exception e) {
 				//
 			//Log.d("DEBUG",e.getMessage());
 		}
 		
		return strData.substring(iRealStart, iRealEnd);
	} 
	
	public String AddZero(String rstr, int number){
		String tmpstr = ""; 
		String rtn  = "";
		int strlen	= rstr.length();
		
		if(strlen>number){
			rtn = rstr.substring(0, number);
		}else{
			for(int i=0; i<(number-strlen); i++){
				tmpstr += "0";
			}
			
			rtn = tmpstr+rstr;
		}
		return rtn;
	}
	
	public static String implode(String separator, String[] data) {
	    StringBuilder sb = new StringBuilder();
	    try {
		    for (int i = 0; i < data.length; i++) {
		    	if(null!=data[i]) {
		    		sb.append(data[i]);
		    		if(null!=data[i+1]) {
			            sb.append(separator);
			    	}
		    	}
		    }
		} catch (Exception e) {
		}
	    
	    return sb.toString();
	}

	/**
	 * 현금영수증 카드번호에 0~9, = 을 제외한 입력을 제거하기 위한 함수
	 * 20151022 장도현
	 * @param valu	: 문자열 입력
	 * @return		: 제거된 문자열 출력
	 */
	public String arraySearch(String valu){
		
		byte[] CashKey = new byte[] {0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3D};
		byte[] srcArr = valu.getBytes();
		byte[] decArr = new byte[srcArr.length];
		int temp = 0;
		for(int s=0;s<srcArr.length;s++){
		
			for(int i=0; i<CashKey.length;i++){
				if(CashKey[i]==srcArr[s]){
					decArr[temp]= srcArr[s];
					temp++;
					break;
				}
			}
			
			
		}
		return new String(decArr).trim();
	}
	
	public static String getKocesCardCode(String acq) {
	    String rtn = "";

	    if (acq.equals("1101"))//국민카드
	      rtn = "16";
	    else if (acq.equals("1102"))//현대카드
	      rtn = "27";
	    else if (acq.equals("1103"))//롯데카드
	      rtn = "47";
	    else if (acq.equals("1104"))//삼성카드
	      rtn = "31";
	    else if (acq.equals("1105"))//외환카드
	      rtn = "08";
	    else if (acq.equals("1106"))//비씨카드
	      rtn = "26";
	    else if (acq.equals("1107"))//신한카드
	      rtn = "29";
	    else if (acq.equals("1180"))//해외비자
	      rtn = "22";
	    else if (acq.equals("1181"))//해외마스터
	      rtn = "78";
	    else if (acq.equals("1182"))//JCB
	      rtn = "28";
	    else if (acq.equals("2207"))//수협
	      rtn = "17";
	    else if (acq.equals("2211"))//농협
	      rtn = "18";
	    else if (acq.equals("2227"))//한미
	      rtn = "19";
	    else if (acq.equals("2234"))//광주
	      rtn = "02";
	    else if (acq.equals("2235"))//제주
	      rtn = "11";
	    else if (acq.equals("2237"))//전북
	      rtn = "10";
	    else if (acq.equals("2281")) {//하나SK
	      rtn = "06";
	    }

	    return rtn;
	  }
	public String getDauCardCode(String strParamCardCd)
	{
		String strReturn = "";
		
		if(strParamCardCd.equals("001")){//bc
			strReturn = "01";
		}else if(strParamCardCd.equals("002")){//국민
			strReturn = "02";
		}else if(strParamCardCd.equals("005")){//외환
			strReturn = "03";
		}else if(strParamCardCd.equals("003")){//삼성
			strReturn = "04";
		}else if(strParamCardCd.equals("004")){//신한
			strReturn = "05";
		}else if(strParamCardCd.equals("008")){//현대
			strReturn = "12";
		}else if(strParamCardCd.equals("094")){//해외아맥스
			strReturn = "40";
		}else if(strParamCardCd.equals("016")){//NH카드
			strReturn = "15";
		}else if(strParamCardCd.equals("007")){//롯데
			strReturn = "13";
		}else if(strParamCardCd.equals("010")){//하나SK
			strReturn = "03";
		}else if(strParamCardCd.equals("092")){//해외 VISA
			strReturn = "40";
		}else if(strParamCardCd.equals("093")){//해외MASTER
			strReturn = "40";
		}else if(strParamCardCd.equals("095")){//해외 DINERS
			strReturn = "40";
		}else if(strParamCardCd.equals("011")){//씨티은행
			strReturn = "05";
		}		
		
		return strReturn;
	}
	
	public String byteArrayToHex(byte[] buffer) {
	    if (buffer == null || buffer.length == 0) {
	        return null;
	    }
	 
	    StringBuffer sb = new StringBuffer(2);
	    String hexNumber;
	    for (int x = 0; x < buffer.length; x++) {
	        hexNumber = "0" + Integer.toHexString(0xff & buffer[x]);
	        sb.append(" "+hexNumber.substring(hexNumber.length() - 2));
	    }
	    return sb.toString();
	}
	
	public String rpadByte(String obj, int len){
		byte[] t1 	= obj.getBytes();
		byte[] t2	= new byte[len];
		
		for(int i=0;i<len;i++){
			if(i<t1.length){
				t2[i] = t1[i];
			}else{
				t2[i] = 0x20;
			}
		}
		
		return new String(t2);
	}
	
	
	
	
	public byte[] hexStringToByteArray(String s) {
		
		byte[] b = new byte[s.length() / 2];
		try {
		    for (int i = 0; i < b.length; i++) {
				int index = i * 2;
				int v = Integer.parseInt(s.substring(index, index + 2), 16);
				b[i] = (byte) v;
		    }
		}catch(Exception e) {}
	    return b;
	  }
	
	public String cardno_masking(String cardno) {
		String rtn = "";
		try {
			
			String[] exp = cardno.trim().split("=");
			
			if(exp[0].length()>10){
				rtn = exp[0].substring(0, 6) + "******" + exp[0].substring(12, exp[0].length());
			}
		} catch (Exception e) {
			rtn = cardno;
		}
		
		return rtn;
	}

	/**
	 * 승인시 부가세, 취소시 원승인번호를 셋업
	 * @param expd
	 * @return
	 */
	public String card_oappno_chk(String[] expd) {
		String rtn = "";
		try{
			if("0".equals(expd[1])){
				rtn = expd[8];
			}else if("1".equals(expd[1])){
				rtn = expd[9];
			}
		}catch(Exception e){}
		return rtn;
	}

	/**
	 * 승인시 봉사료, 취소시 원승인일자를 셋업
	 * @param expd
	 * @return
	 */
	public String card_oappdd_chk(String[] expd) {
		String rtn = "";
		try{
			if("0".equals(expd[1])){
				rtn = expd[7];
			}else if("1".equals(expd[1])||"8".equals(expd[1])){
				if(expd[10].length()<8){
					rtn = "20"+expd[10];
				}else{
					rtn = expd[10];
				}
			}
		}catch(Exception e){}
		return rtn;
	}
	
	public String auth_convert(String authcd){
		String rtn = "";
		try {
			if(authcd.equals("0")){
				rtn = "A";
			}else if(authcd.equals("1")){
				rtn = "C";
			}else if(authcd.equals("7")){
				rtn = "A";
			}else if(authcd.equals("8")){
				rtn = "C";
			}else{
				rtn = "F";
			}
		} catch (Exception e) {
		}		
		return rtn;
	}

	public String cardtype(String ctype) {
		String rtn = "";
		try {
			if(ctype.equals("Y")){
				rtn = "1";
			}else if(ctype.equals("N")){
				rtn = "2";
			}else{
				rtn = "4";
			}
		} catch (Exception e) {
		}
		return rtn;
	}
	
	public String strcardtype(String ctype) {
		String rtn = "";
		try {
			if(ctype.equals("Y")){
				rtn = "체크카드";
			}else{
				rtn = "일반카드";
			}
		} catch (Exception e) {
		}
		return rtn;
	}
	
	public String strsigngb(String ctype) {
		String rtn = "";
		try {
			if(ctype.equals("1")){
				rtn = "전자서명";
			}else{
				rtn = "무서명";
			}
		} catch (Exception e) {
		}
		return rtn;
	}
	
	public String str_to_dateformat(String in) {
		String rtn = "";
		StringBuffer strbuf = new StringBuffer();
		try {
			if(in.trim().length()==8){
				strbuf.append(in.substring(0, 4));
				strbuf.append("/");
				strbuf.append(in.substring(4, 6));
				strbuf.append("/");
				strbuf.append(in.substring(6, 8));
				
				rtn = strbuf.toString();
			}else{
				//rtn = in;
			}
		} catch (Exception e) {
		}finally {
			strbuf = null;
		}
		return rtn;
	}
	
	//2021.02.26 매입매출쪽 날짜표기
	public String str_to_dateformat_deposit(String in) {
		String rtn = "";
		StringBuffer strbuf = new StringBuffer();
		try {
			if(in.trim().length()==8){
				strbuf.append(in.substring(0, 4));
				strbuf.append("-");
				strbuf.append(in.substring(4, 6));
				strbuf.append("-");
				strbuf.append(in.substring(6, 8));
				
				rtn = strbuf.toString();
			}else{
				//rtn = in;
			}
		} catch (Exception e) {
		}finally {
			strbuf = null;
		}
		return rtn;
	}
	
	
	public String deposit_rst_to_kor(String in) {
		String rtn = "";
		StringBuffer strbuf = new StringBuffer();
		try {
			if(in.equals("60") || in.equals("67")){
				rtn = "정상매입";
			}else if(in.equals("61") || in.equals("64")){
				rtn = "매입반송";
			}else{
				//rtn = in;
			}
		} catch (Exception e) {
		}finally {
			strbuf = null;
		}
		return rtn;
	}
	
	public String str_to_timeformat(String in) {
		String rtn = "";
		StringBuffer strbuf = new StringBuffer();
		try {
			if(in.trim().length()==6){
				strbuf.append(in.substring(0, 2));
				strbuf.append(":");
				strbuf.append(in.substring(2, 4));
				strbuf.append(":");
				strbuf.append(in.substring(4, 6));
				
				rtn = strbuf.toString();
			}else{
				//rtn = in;
			}
		} catch (Exception e) {
		}finally {
			strbuf = null;
		}
		return rtn;
	}
	
	
	public String set_appgb_to_kor(String in) {
		String rtn = "";
		try {
			if(in.trim().equals("A")){
				rtn = "신용승인";
			}else if(in.trim().equals("C")){
				rtn = "승인취소";
			}
		} catch (Exception e) {
		}finally {
		}
		return rtn;
	}
	
	public static String setDefault(String str) 
	{
		String rtnstr = "";
		try {
			if(str != null) {
				rtnstr = str;
			}else {
				rtnstr = "";
			}
		} catch (Exception e) {
		}
		return rtnstr;
	}
	
	public static String getTimestamp()
	{
		String rtnstr = "";
		try {
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
			Calendar cal = Calendar.getInstance();
			String today = null;
			today = formatter.format(cal.getTime());
			Timestamp ts = Timestamp.valueOf(today);
			
			rtnstr = ts.toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return rtnstr;
	}

	public String deposit_hold_check(String state) {
		String rtnstr = "";
		try {
			if(state.equals("RV01")) {
				rtnstr = "1";
			}else{
				rtnstr = "0";
			}
		} catch (Exception e) {
		}
		return rtnstr;
	}

	public String deposit_state_check(String state) {
		String rtnstr = "";
		try {
			if(state.equals("RV01")) {
				rtnstr = "보류";
			}else if(state.equals("RQ01")) {
				rtnstr = "청구완료";
			}else if(state.equals("DP01")) {
				rtnstr = "매입완료";
			}else if(state.equals("DP99")) {
				rtnstr = "매입반송";
			}else {
				rtnstr = "청구대상";
			}
		} catch (Exception e) {
		}
		return rtnstr;
	}

	public String deposit_result_str(String rtncd) {
		String rtnstr = "";
		try {
			if(rtncd.equals("60")) {
				rtnstr = "60:매출정상";
			}else if(rtncd.equals("67")) {
				rtnstr = "67:취소정상";
			}else if(rtncd.equals("62")) {
				rtnstr = "62:매출보류";
			}else if(rtncd.equals("63")) {
				rtnstr = "63:매출보류해제";
			}else if(rtncd.equals("61")) {
				rtnstr = "61:매출반송";
			}else if(rtncd.equals("64")) {
				rtnstr = "64:취소반송";
			}else if(rtncd.equals("65")) {
				rtnstr = "65:취소보류";
			}else if(rtncd.equals("66")) {
				rtnstr = "66:취소보류해제";
			}else {
				rtnstr = "정보없음";
			}
		} catch (Exception e) {
		}
		return rtnstr;
	}

	public void debug_qrybuf(StringBuffer qrybuf) {
		
		System.out.println("========================");
		System.out.println("query check :: ");
		System.out.println(qrybuf.toString());
		System.out.println("========================");
	}
	
	//PreparedStatement 사용 시 쿼리 출력
	public void debug_sql(StringBuffer sql, ArrayList<String> arrParam) throws SQLException {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> sqlArrya = arrParam;
		String strValue;
		try {
			// 쿼리에서 ? 를 찾음
			Pattern pattern = Pattern.compile("\\?");
			Matcher matcher = pattern.matcher(sql.toString());
			int indx = 0;
			// ? 를 바인딩한 값으로 변경
			while (matcher.find()) {
				// 바인딩 값에서 String 타입의 경우 ''를 붙이고 값이 없을경우 null, 그외 값 그대로 넣어줌
				if (sqlArrya.get(indx) instanceof String) {
					strValue = "'" + arrParam.get(indx) + "'";
				} else {
					if (sqlArrya.get(indx) == null) {
						strValue = "null";
					} else {
						strValue = sqlArrya.get(indx).toString();
					}
				}
				matcher.appendReplacement(sb, strValue);
				indx++;
			}
			matcher.appendTail(sb);
			System.out.println("========================");
			System.out.println("query check :: ");
			System.out.println(sb.toString());
			System.out.println("========================");
		} catch (Exception ex) {
			//비정상적인 쿼리인경우 ex)바인딩 할 변수의 개수가 안맞을때 표출
			System.out.println("========================");
			System.out.println("query Error check :: ");
			System.out.println(sql.toString());
			System.out.println("parameter check - ");
			for (int k = 0; k < arrParam.size(); k++) {
				System.out.println((k + 1) + " : " + arrParam.get(k));
			}
			System.out.println("========================");
		}
	}
	
	//로그 테이블 삽입시 where 절 추출
	public String debug_whereqry(StringBuffer sql, ArrayList<String> arrParam) throws SQLException {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> sqlArrya = arrParam;
		String strValue;
		try {
			Pattern pattern = Pattern.compile("\\?");
			Matcher matcher = pattern.matcher(sql.toString());
			int indx = 0;
			while (matcher.find()) {
				if (sqlArrya.get(indx) instanceof String) {
					strValue = "'" + arrParam.get(indx) + "'";
				} else {
					if (sqlArrya.get(indx) == null) {
						strValue = "null";
					} else {
						strValue = sqlArrya.get(indx).toString();
					}
				}
				matcher.appendReplacement(sb, strValue);
				indx++;
			}
			matcher.appendTail(sb);
		} catch (Exception ex) {
		}
		return sb.toString();
	}
	
	//2021.02.16 null 0으로 치환리턴
	public String checkNumberData(String str){
		if(str == null){
			return "0";
		}
		return str;
	}
	
	public String checkNumberData(Double num){
		int nNum = 0;
		if(num == null){
			return "0";
		}else {
			nNum = (int)Math.floor(num);
		}
		return Integer.toString(nNum);
	}

}