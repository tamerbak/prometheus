package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.ScheduleEntry;

public class ScheduleDAO {

	public void insertEntry(CoreUser user,ScheduleEntry e){
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "insert into s_schedule_entry (title, description,start_at,end_at,rappel,rappel_time,priority,user_id)" +
							" values (?,?,?,?,?,?,?,?)";
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setString(1, e.getTitle());
			ps.setString(2, e.getDescription());
			ps.setTimestamp(3, new Timestamp(e.getStartAt().getTime()));
			ps.setTimestamp(4, new Timestamp(e.getEndAt().getTime()));
			ps.setString(5, e.isRappel()?"Y":"N");
			if(e.getRappelAt() == null)
				ps.setDate(6, null);
			else
				ps.setTimestamp(6, new Timestamp(e.getRappelAt().getTime()));
			ps.setInt(7, e.getPriority());
			ps.setInt(8, user.getId());
			
			ps.execute();
			ps.close();
			
			sql = "select nextval('s_sched_seq')";
			ps = cnx.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				e.setId(rs.getInt(1)-1);
			
			rs.close();
			ps.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public List<ScheduleEntry> loadSchedule(CoreUser user){
		List<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "select id,title, description,start_at,end_at,rappel,rappel_time,priority from s_schedule_entry where user_id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, user.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				ScheduleEntry e = new ScheduleEntry();
				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setDescription(rs.getString(3));
				e.setStartAt(rs.getDate(4));
				e.setEndAt(rs.getDate(5));
				e.setRappel(rs.getString(6).equals("Y"));
				e.setRappelAt(rs.getDate(7));
				e.setPriority(rs.getInt(8));
				entries.add(e);
			}
			
			rs.close();
			ps.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return entries;
	}
	
	public void deleteEntry(ScheduleEntry entry){
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "delete from s_schedule_entry where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			ps.setInt(1, entry.getId());
			
			ps.execute();
			ps.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void updateEntry(ScheduleEntry e){
		try{
			Connection cnx=ProtogenConnection.getInstance().getConnection();
			String sql = "update s_schedule_entry set title=?, description=?,start_at=?,end_at=?,rappel=?,rappel_time=?,priority=?" +
							" where id=?";
			PreparedStatement ps = cnx.prepareStatement(sql);
			
			ps.setString(1, e.getTitle());
			ps.setString(2, e.getDescription());
			ps.setTimestamp(3, new Timestamp(e.getStartAt().getTime()));
			ps.setTimestamp(4, new Timestamp(e.getEndAt().getTime()));
			ps.setString(5, e.isRappel()?"Y":"N");
			if(e.getRappelAt() == null)
				ps.setDate(6, null);
			else
				ps.setTimestamp(6, new Timestamp(e.getRappelAt().getTime()));			
			ps.setInt(7, e.getPriority());
			ps.setInt(8, e.getId());
			
			ps.execute();
			ps.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
