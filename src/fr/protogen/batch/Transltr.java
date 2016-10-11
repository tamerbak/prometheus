package fr.protogen.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.io.IOUtils;

import fr.protogen.masterdata.dbutils.DBUtils;

public class Transltr {

	public static void main(String[] args) {
		/*exportMenu();
		exportActions();
		exportAttributes();
		exportDocs();
		exportRubrique();
		exportTables();
		exportWindow();
		*/
		importMenu();
		importActions();
		importAttributes();
		importDocs();
		importRubrique();
		importTables();
		importWindow();

	}

	public static void exportMenu(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\menu.csv";
			File f = new File(path);
			String content = "";
			String sql = "select id, title from s_menuitem";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				content= content+rs.getInt(1)+";"+rs.getString(2)+"\n";
			rs.close();
			ps.close();
			cnx.close();
			OutputStream out = new FileOutputStream(f);
			IOUtils.write(content, out);
			out.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void importMenu(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\menu.csv";
			File f = new File(path);
			List<String> lines = IOUtils.readLines(new FileInputStream(f));
			String sql = "insert into c_menu_trans (id_menu, code_lang, val) values (?,?,?)";
			for(String l : lines){
				int id = Integer.parseInt(
							l.split(";")[0]
						);
				String val = l.split(";")[1].replace("\n", "");
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setString(2, "en");
				ps.setString(3, val);
				ps.execute();
				ps.close();
			}
			cnx.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void exportRubrique(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\rubrique.csv";
			File f = new File(path);
			String content = "";
			String sql = "select id, title from s_rubrique";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				content= content+rs.getInt(1)+";"+rs.getString(2)+"\n";
			rs.close();
			ps.close();
			cnx.close();
			OutputStream out = new FileOutputStream(f);
			IOUtils.write(content, out);
			out.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void importRubrique(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\rubrique.csv";
			File f = new File(path);
			List<String> lines = IOUtils.readLines(new FileInputStream(f));
			String sql = "insert into c_rubrique_trans (id_rubrique, code_lang, val) values (?,?,?)";
			for(String l : lines){
				int id = Integer.parseInt(
							l.split(";")[0]
						);
				String val = l.split(";")[1].replace("\n", "");
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setString(2, "en");
				ps.setString(3, val);
				ps.execute();
				ps.close();
			}
			cnx.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void exportWindow(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\fenetres.csv";
			File f = new File(path);
			String content = "";
			String sql = "select id, title from c_window";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				content= content+rs.getInt(1)+";"+rs.getString(2)+"\n";
			rs.close();
			ps.close();
			cnx.close();
			OutputStream out = new FileOutputStream(f);
			IOUtils.write(content, out);
			out.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void importWindow(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\fenetres.csv";
			File f = new File(path);
			List<String> lines = IOUtils.readLines(new FileInputStream(f));
			String sql = "insert into c_window_trans (id_window, code_lang, val) values (?,?,?)";
			for(String l : lines){
				int id = Integer.parseInt(
							l.split(";")[0]
						);
				String val = l.split(";")[1].replace("\n", "");
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setString(2, "en");
				ps.setString(3, val);
				ps.execute();
				ps.close();
			}
			cnx.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void exportTables(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\tables.csv";
			File f = new File(path);
			String content = "";
			String sql = "select id, name from c_businessclass";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				content= content+rs.getInt(1)+";"+rs.getString(2)+"\n";
			rs.close();
			ps.close();
			cnx.close();
			OutputStream out = new FileOutputStream(f);
			IOUtils.write(content, out);
			out.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void importTables(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\tables.csv";
			File f = new File(path);
			List<String> lines = IOUtils.readLines(new FileInputStream(f));
			String sql = "insert into c_table_trans (id_class, code_lang, val) values (?,?,?)";
			for(String l : lines){
				int id = Integer.parseInt(
							l.split(";")[0]
						);
				String val = l.split(";")[1].replace("\n", "");
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setString(2, "en");
				ps.setString(3, val);
				ps.execute();
				ps.close();
			}
			cnx.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void exportAttributes(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\attributes.csv";
			File f = new File(path);
			String content = "";
			String sql = "select id, attribute from c_attribute";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				content= content+rs.getInt(1)+";"+rs.getString(2)+"\n";
			rs.close();
			ps.close();
			cnx.close();
			OutputStream out = new FileOutputStream(f);
			IOUtils.write(content, out);
			out.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void importAttributes(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\attributes.csv";
			File f = new File(path);
			List<String> lines = IOUtils.readLines(new FileInputStream(f));
			String sql = "insert into c_attribute_trans (id_attribute, code_lang, val) values (?,?,?)";
			for(String l : lines){
				int id = Integer.parseInt(
							l.split(";")[0]
						);
				String val = l.split(";")[1].replace("\n", "");
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setString(2, "en");
				ps.setString(3, val);
				ps.execute();
				ps.close();
			}
			cnx.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void exportActions(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\actions.csv";
			File f = new File(path);
			String content = "";
			String sql = "select id, title from c_actionbutton";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				content= content+rs.getInt(1)+";"+rs.getString(2)+"\n";
			rs.close();
			ps.close();
			cnx.close();
			OutputStream out = new FileOutputStream(f);
			IOUtils.write(content, out);
			out.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void importActions(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\actions.csv";
			File f = new File(path);
			List<String> lines = IOUtils.readLines(new FileInputStream(f));
			String sql = "insert into c_action_trans (id_action, code_lang, val) values (?,?,?)";
			for(String l : lines){
				int id = Integer.parseInt(
							l.split(";")[0]
						);
				String val = l.split(";")[1].replace("\n", "");
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setString(2, "en");
				ps.setString(3, val);
				ps.execute();
				ps.close();
			}
			cnx.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void exportDocs(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\documents.csv";
			File f = new File(path);
			String content = "";
			String sql = "select id, title from c_documentbutton";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				content= content+rs.getInt(1)+";"+rs.getString(2)+"\n";
			rs.close();
			ps.close();
			cnx.close();
			OutputStream out = new FileOutputStream(f);
			IOUtils.write(content, out);
			out.close();
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
	
	public static void importDocs(){
		try {
			Class.forName("org.postgresql.Driver");

		    Connection cnx = DriverManager.getConnection(DBUtils.url,DBUtils.username, DBUtils.password);
			
			String path="D:\\documents.csv";
			File f = new File(path);
			List<String> lines = IOUtils.readLines(new FileInputStream(f));
			String sql = "insert into c_document_trans (id_document, code_lang, val) values (?,?,?)";
			for(String l : lines){
				int id = Integer.parseInt(
							l.split(";")[0]
						);
				String val = l.split(";")[1].replace("\n", "");
				PreparedStatement ps = cnx.prepareStatement(sql);
				ps.setInt(1, id);
				ps.setString(2, "en");
				ps.setString(3, val);
				ps.execute();
				ps.close();
			}
			cnx.close();
			
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}
}
