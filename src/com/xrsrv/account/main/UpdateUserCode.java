package com.xrsrv.account.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UpdateUserCode {
	private static final Log log = LogFactory.getLog(UpdateUserCode.class);
	
	static Properties prop = new Properties();
	
	static{
		try {
			//从classpath下加载crmInvestor.properties文件
			prop.load(UpdateUserCode.class.getResourceAsStream("/code.properties"));
			Class.forName(prop.getProperty("jdbc.driver-class"));	//加载驱动(系统中只会加载一次)
		} catch (Exception e) {
			e.printStackTrace();
			log.error("----load code.properties fialed,system exit!!!",e); 
			System.exit(1);
		}
	}
	
	private UpdateUserCode(){}
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

	public static void main(String[] args){
		insert();
//		method1();
		method2();
	}
	
	public static void insert(){
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = DriverManager.getConnection(prop.getProperty("jdbc.driver-url"), 
											   prop.getProperty("jdbc.user"), 
											   prop.getProperty("jdbc.password")); 
			StringBuilder sql = new StringBuilder(32);
			sql.append("insert into acc_user_reg(au_id, login_name, password, user_type, recommend_mobile, status,")
			   .append("   	   reg_time,auditor, audit_time,mobile,allow_borrow)")
			   .append("    select aur.au_id, aur.login_name, aur.password, aur.user_type, aur.recommend_mobile, aur.status,")
			   .append(" 		   aur.reg_time, aur.auditor, aur.audit_time, nvl(aui.mobile,aoi.mobile),nvl2(cu.role_type,1,0)")
			   .append("      from banhui.acc_user_reg aur")
			   .append(" left join banhui.acc_user_info aui on aui.au_id = aur.au_id")
			   .append(" left join banhui.acc_org_info aoi on aoi.au_id = aur.au_id")
			   .append(" left join banhui.cp_user cu on aur.au_id = cu.cu_id and cu.role_type = 2");
			ps = conn.prepareStatement(sql.toString());
			ps.execute();
			ps.close();
			
			sql.delete(0, sql.length());
			sql.append("insert into acc_person_info(au_id, address, home_phone, postal_code, position, company, company_type, ")
			   .append(" 	qq_number, update_time, create_time, real_name, id_card, email, show_name,org_code) ")
			   .append("select au_id, address,home_phone, postal_code, position, company, company_type,  qq_number,")
			   .append(" 	update_time,  create_time, real_name, id_card, email, show_name, org_code ")
			   .append("  from banhui.acc_user_info");
			ps = conn.prepareStatement(sql.toString());
			ps.execute();
			ps.close();
			
			sql.delete(0, sql.length());
			sql.append("insert into acc_corp_info(au_id,org_name, buss_lic, tax_lic, org_code_no, law_name, law_id_card,")
			   .append("		acc_user_name, account, acc_bank, create_time,update_time,real_name, position, id_card,")
			   .append("		company, company_type, address, postal_code, home_phone, qq_number, email, show_name)")
			   .append("select au_id, org_name, buss_lic, tax_lic, org_code_no, law_name, law_id_card, acc_username, account, acc_bank,")
			   .append("	   create_time, update_time, real_name, position, id_card, company, company_type, address,")
			   .append("	   postal_code, home_phone, qq_number,email,show_name")
			   .append("  from banhui.acc_org_info");
			ps = conn.prepareStatement(sql.toString());
			ps.execute();
			ps.close();
			conn.commit();
		}catch(Exception e){
			e.printStackTrace();
			log.error("update acc_user_reg user_code",e);
		}finally{
			colse(conn, ps, null);
			log.info("insert end!");
			System.out.println("insert end!");
		}
	}
	
	public static void method1(){
		Connection conn = null;
		PreparedStatement ps = null;
		
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		try{
			conn = DriverManager.getConnection(prop.getProperty("jdbc.driver-url"), 
											   prop.getProperty("jdbc.user"), 
											   prop.getProperty("jdbc.password")); 
			
			StringBuilder sql = new StringBuilder(32); 
			sql.append("update acc_user_reg aur set aur.user_code = ? ")
			   .append(" where  au_id = ? and not exists (select 1 from acc_user_reg where user_code = ?)"); 
			//(user_code = 0 or user_code is null)
			ps = conn.prepareStatement(sql.toString()); 
			
			ps2 = conn.prepareStatement("select aur.user_type, aur.reg_time, aur.au_id from acc_user_reg aur where aur.user_code = 0 or aur.user_code is null");
			rs2 = ps2.executeQuery();
			String userCode = "";
			while(rs2.next()){
				//产生新userCode
				int user_type = rs2.getInt(1);
				Date date = rs2.getDate(2);
				int id = rs2.getInt(3);
				userCode = "2"+user_type+df.format(date)+random();
				
				//更新usreCode:不能用批处理，须一条一条判断及插入
				int cl = 0;
				while(cl == 0){
					userCode = "2"+user_type+df.format(date)+random();
					ps.setString(1, userCode);
					ps.setInt(2, id);
					ps.setString(3, userCode);
					cl = ps.executeUpdate();
				}
			}
			conn.commit();
		}catch(Exception e){
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				log.error("----rollback failed!",e);
			}
			e.printStackTrace();
			log.error("update acc_user_reg user_code",e);
			
		}finally{
			colse(null,ps2,rs2);
			colse(conn,ps,null);
			log.info("method1 run end!"); 
			System.out.println("method1 run end!");
		}
	}
	
	/**
	 * 更新用户USER_CODE
	 * 说明:必须先在数据库中对user_code建立唯一约束(允许为null),再调用该方法。否则有可能出现重复user_code的情况。
	 * 原因:通过数据库的唯一约束限制数据的插入。
	 * @throws SQLException
	 */
	private static void method2() {
		StringBuilder sql = new StringBuilder();
		sql.append("update acc_user_reg aur ")
		   .append("   set aur.user_code = '2'||aur.user_type||to_char(aur.reg_time,'yyyymmdd')||substr(dbms_random.value,2,6)")
		   .append(" where aur.user_code  = 0 or aur.user_code is null");
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = DriverManager.getConnection(prop.getProperty("jdbc.driver-url"), 
					   prop.getProperty("jdbc.user"), 
					   prop.getProperty("jdbc.password")); 
			ps = conn.prepareStatement(sql.toString()); 
			while(true){
				try{
					ps.executeUpdate();
					break;
				}catch(Exception e){
					log.error(e);
					e.printStackTrace();
				}
			}
			conn.commit();
		}catch(Exception e){
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException e1) {
				log.error("----rollback failed!",e);
			}
			e.printStackTrace();
			log.error("update acc_user_reg user_code",e);
		}finally{
			colse(conn,ps,null);
			log.info("method2 run end!");  
			System.out.println("method2 run end!");
		}
	}
	
	private static void colse(Connection conn,PreparedStatement ps,ResultSet rs){
		try{
			if(rs != null)
				rs.close();
		}catch (SQLException e) {
			log.error("--close ResultSet failed!",e);
		}
		
		try {
			if(ps != null)
				ps.close();
		} catch (SQLException e) {
			log.error("--close PreparedStatement failed!",e);
		}
		
		try {
			if(conn != null)
				conn.close();
		} catch (SQLException e) {
			log.error("--close Connection failed!",e);
		}
	}
	
	private static String random(){
		Random random = new Random();
		String code = "";
		for(int i = 0; i < 6; i++){
			code += random.nextInt(10);
		}
		return code;
	}
}
