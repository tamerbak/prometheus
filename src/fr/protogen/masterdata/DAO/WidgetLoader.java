package fr.protogen.masterdata.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;

import fr.protogen.engine.utils.PairKVElement;
import fr.protogen.masterdata.dbutils.DBUtils;
import fr.protogen.masterdata.dbutils.ProtogenConnection;
import fr.protogen.masterdata.model.SWidget;

public class WidgetLoader {
	public List<SWidget> getWidgetsApplication(String appKey, int userId) {

		String widgetQuery = "select w.* from s_widget w where w.appkey='"
				+ appKey + "' order by id asc";
		String widgetUserQuery = "select w.id_widget from s_widget_user w where w.id_user="
				+ userId + " order by id asc";
		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = ProtogenConnection.getInstance().getConnection();// DBUtils.ds.getConnection();
			Statement st1 = cnx.createStatement();
			ResultSet rsWU = st1.executeQuery(widgetUserQuery);
			List<Integer> widgetIds = new ArrayList<Integer>();
			while (rsWU.next()) {
				widgetIds.add(rsWU.getInt(1));
			}

			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(widgetQuery);
			List<SWidget> widgets = new ArrayList<SWidget>();

			while (rs.next()) {

				SWidget widget = new SWidget();
				widget.setId(rs.getInt("id"));
				widget.setTitle(rs.getString("title"));
				widget.setLabel(rs.getString("label"));
				widget.setType(rs.getString("type").charAt(0));
				widget.setQuery(rs.getString("wquery"));
				widget.setLvalues(rs.getString("lvalue"));
				widget.setToShow(widgetIds.contains(widget.getId()));
				widget.setToDel(rs.getObject("created_by")!= null && rs.getInt("created_by") == userId);

				widgets.add(widget);
			}
			rs.close();
			rsWU.close();

			for (SWidget widget : widgets) {
				if (widget.getType() == 'P') {
					// Pie chart
					ResultSet wrs = st.executeQuery(widget.getQuery());
					widget.setModel(new PieChartModel());
					while (wrs.next()) {
						widget.getModel().set(wrs.getString("wlabel"),
								wrs.getDouble("wvalue"));
						widget.getPieData().add(
								new PairKVElement(wrs.getString("wlabel"), wrs
										.getString("wvalue")));
					}
				} else if (widget.getType() == 'C') {
					// Bar chart
					ResultSet wrs = st.executeQuery(widget.getQuery());
					widget.setLineModel(new CartesianChartModel());
					ChartSeries serie = new ChartSeries();
					serie.setLabel(widget.getTitle());
					double max = 0;
					while (wrs.next()) {
						serie.set(wrs.getString("wlabel"),
								wrs.getDouble("wvalue"));
						if (wrs.getDouble("wvalue") > max)
							max = wrs.getDouble("wvalue");
					}
					max = Math.round(max * 1.10);
					widget.getLineModel().addSeries(serie);
					widget.setMax(max);
				} else if (widget.getType() == 'T') {
					ResultSet wrs = st.executeQuery(widget.getQuery());
					widget.setDataTable(new ArrayList<List<String>>());
					String[] labels = widget.getLabel().split(";");
					List<String> titres = new ArrayList<String>();
					for (String l : labels) {
						titres.add(l);
					}
					widget.setDataCaptions(titres);
					String[] sqlLabels = widget.getLvalues().split(";");
					while (wrs.next()) {
						List<String> row = new ArrayList<String>();
						for (String l : sqlLabels) {
							String k = wrs.getObject(l).toString();
							if (k.split(" ").length == 2
									&& k.split("-").length == 3) // Date
								k = k.split(" ")[0];

							row.add(k);
						}
						widget.getDataTable().add(row);
					}

				}
			}

			st.close();

			return widgets;
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return null;

	}

	public List<SWidget> getWidgets(int userId, String appKey) {

		String widgetQuery = "select w.* from s_widget as w, s_widget_user wu where appkey='"
				+ appKey
				+ "' and w.id=wu.id_widget and wu.id_user='"
				+ userId
				+ "'  order by id asc";

		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx = ProtogenConnection.getInstance().getConnection();// DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(widgetQuery);
			List<SWidget> widgets = new ArrayList<SWidget>();

			while (rs.next()) {
				SWidget widget = new SWidget();
				widget.setId(rs.getInt("id"));
				widget.setTitle(rs.getString("title"));
				widget.setLabel(rs.getString("label"));
				widget.setType(rs.getString("type").charAt(0));
				widget.setQuery(rs.getString("wquery"));
				widget.setLvalues(rs.getString("lvalue"));

				widgets.add(widget);
			}

			rs.close();

			for (SWidget widget : widgets) {
				if (widget.getType() == 'P') {
					// Pie chart
					ResultSet wrs = st.executeQuery(widget.getQuery());
					widget.setModel(new PieChartModel());
					while (wrs.next()) {
						widget.getModel().set(wrs.getString("wlabel"),
								wrs.getDouble("wvalue"));
						widget.getPieData().add(
								new PairKVElement(wrs.getString("wlabel"), wrs
										.getString("wvalue")));
					}
				} else if (widget.getType() == 'C') {
					// Bar chart
					ResultSet wrs = st.executeQuery(widget.getQuery());
					widget.setLineModel(new CartesianChartModel());
					ChartSeries serie = new ChartSeries();
					serie.setLabel(widget.getTitle());
					double max = 0;
					while (wrs.next()) {
						serie.set(wrs.getString("wlabel"),
								wrs.getDouble("wvalue"));
						if (wrs.getDouble("wvalue") > max)
							max = wrs.getDouble("wvalue");
					}
					max = Math.round(max * 1.10);
					widget.getLineModel().addSeries(serie);
					widget.setMax(max);
				} else if (widget.getType() == 'T') {
					ResultSet wrs = st.executeQuery(widget.getQuery());
					widget.setDataTable(new ArrayList<List<String>>());
					String[] labels = widget.getLabel().split(";");
					List<String> titres = new ArrayList<String>();
					for (String l : labels) {
						titres.add(l);
					}
					widget.setDataCaptions(titres);
					String[] sqlLabels = widget.getLvalues().split(";");
					while (wrs.next()) {
						List<String> row = new ArrayList<String>();
						for (String l : sqlLabels) {
							String k = wrs.getObject(l).toString();
							if (k.split(" ").length == 2
									&& k.split("-").length == 3) // Date
								k = k.split(" ")[0];

							row.add(k);
						}
						widget.getDataTable().add(row);
					}

				}
			}

			st.close();

			return widgets;
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return null;

	}

	public void deleteUserWidgets(Connection cnx, List<SWidget> sWidgets,
			int userId, String appkey) {
		String qDeleteWidget = "DELETE FROM s_widget_user WHERE id_user="
				+ userId + " and id_widget IN(";
		for (Iterator<SWidget> it = sWidgets.iterator(); it.hasNext();) {
			SWidget widget = it.next();
			qDeleteWidget += widget.getId();
			if (it.hasNext()) {
				qDeleteWidget += ",";
			}
		}
		qDeleteWidget += ");";

		try {
			Class.forName("org.postgresql.Driver");

			// Connection cnx =
			// ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			Statement st = cnx.createStatement();
			st.execute(qDeleteWidget);
			st.close();
			// cnx.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateWidget(List<SWidget> sWidgets, int userId, String appkey)
			throws Exception {

		String qInsertWidget = "INSERT INTO s_widget_user (id_widget, id_user) VALUES(?,?)";
		Connection cnx = null;
		PreparedStatement widgetStatement = null;
		try {
			Class.forName("org.postgresql.Driver");

			cnx = ProtogenConnection.getInstance().getConnection();
			cnx.setAutoCommit(false);
			deleteUserWidgets(cnx, sWidgets, userId, appkey);
			for (SWidget widget : sWidgets) {
				if (widget.getToShow()) {
					widgetStatement = cnx.prepareStatement(qInsertWidget);
					widgetStatement.setInt(1, widget.getId());
					widgetStatement.setInt(2, userId);
					widgetStatement.executeUpdate();
					cnx.commit();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (widgetStatement != null) {
				widgetStatement.close();
			}
			cnx.setAutoCommit(true);

		}
	}

	public SWidget createWidget(SWidget widget, int userId, String appkey)
			throws SQLException {
		String qInsertWidget = "INSERT INTO s_widget (title, type, wquery, appkey, created_by) VALUES(?,?,?,?,?)";
		String qInsertWidgetUser = "INSERT INTO s_widget_user (id_widget, id_user) VALUES(?,?)";
		Connection cnx = null;
		PreparedStatement widgetUserStatement = null;
		PreparedStatement widgetStatement = null;
		try {
			Class.forName("org.postgresql.Driver");

			cnx = ProtogenConnection.getInstance().getConnection();
			cnx.setAutoCommit(false);
			widgetStatement = cnx.prepareStatement(qInsertWidget);
			widgetStatement.setString(1, widget.getTitle());
			widgetStatement.setString(2, String.valueOf(widget.getType()));
			widgetStatement.setString(3, widget.getQuery());
			widgetStatement.setString(4, appkey);
			widgetStatement.setInt(5, userId);
			widgetStatement.executeUpdate();

			String query = "select nextval('s_widget_seq')";
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(query);
			int widgetId = 0;
			if (rs.next())
				widgetId = rs.getInt(1) - 1;
			rs.close();
			widget.setId(widgetId);
			widgetUserStatement = cnx.prepareStatement(qInsertWidgetUser);
			widgetUserStatement.setInt(1, widgetId);
			widgetUserStatement.setInt(2, userId);
			widgetUserStatement.executeUpdate();
			cnx.commit();
			return widget;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (widgetUserStatement != null) {
				widgetUserStatement.close();
			}
			cnx.setAutoCommit(true);

		}

	}

	public String getTypedashboard(int userId, String appKey) {

		Connection cnx = null;
		String query = "SELECT type FROM s_dashboard_user WHERE id_user="+ userId + " AND appkey='" + appKey + "' ORDER BY id ASC";
		String dashboardType = "list";
		try {
			Class.forName("org.postgresql.Driver");

			cnx = ProtogenConnection.getInstance().getConnection();// DBUtils.ds.getConnection();
			Statement st1 = cnx.createStatement();
			ResultSet rsWU = st1.executeQuery(query);
			while (rsWU.next()) {
				dashboardType = rsWU.getString("type");
			}
			dashboardType = dashboardType==null?"list":dashboardType;
			st1.close();
			rsWU.close();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return dashboardType;
	}

	public void saveDashboard(String typeDashboard, int userId, String appkey) throws SQLException {
		String qSf = "select id, type from s_dashboard_user WHERE id_user="+userId+" AND  appkey='"+appkey+"'";
		String qInsertWidget = "UPDATE s_dashboard_user SET type = '"+typeDashboard+"' WHERE id_user="+userId+" AND  appkey='"+appkey+"'";
		Connection cnx = null;
		Statement dashboardStatement = null;
		try {
			Class.forName("org.postgresql.Driver");

			cnx = ProtogenConnection.getInstance().getConnection();
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery(qSf);
			if(!rs.next()){
				qInsertWidget = "INSERT INTO s_dashboard_user (id_user, appkey, type) VALUES("+userId+",'"+appkey+"','"+typeDashboard+"')";
			}
			st.close();
			rs.close();
			cnx.setAutoCommit(false);
			
			dashboardStatement = cnx.createStatement();
//			dashboardStatement.setInt(1, userId);
//			dashboardStatement.setString(2, appkey);
			dashboardStatement.execute(qInsertWidget);
			cnx.commit();
				
			}catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (dashboardStatement != null) {
					dashboardStatement.close();
				}
				cnx.setAutoCommit(true);

			}
		
	}

	public void deleteWidget(SWidget w, int userId, String appkey) {
		String qDeleteWidgetUser = "DELETE FROM s_widget_user WHERE id_user="+ userId + " and id_widget ="+w.getId();
		String qDeleteWidget = "DELETE FROM s_widget WHERE created_by="+ userId + " and id ="+w.getId();

		try {
			Class.forName("org.postgresql.Driver");

			Connection cnx =ProtogenConnection.getInstance().getConnection();//DBUtils.ds.getConnection();
			
			Statement st = cnx.createStatement();
			st.execute(qDeleteWidgetUser);
			st.close();
			
			st = cnx.createStatement();
			st.execute(qDeleteWidget);
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
}
