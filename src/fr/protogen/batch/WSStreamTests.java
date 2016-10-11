package fr.protogen.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.thoughtworks.xstream.XStream;

import fr.protogen.connector.model.AmanToken;
import fr.protogen.connector.model.DataCouple;
import fr.protogen.connector.model.DataEntry;
import fr.protogen.connector.model.DataModel;
import fr.protogen.connector.model.DataRow;
import fr.protogen.connector.model.GeneriumStructure;
import fr.protogen.connector.model.SearchClause;

public class WSStreamTests {

	public static void main(String[] args) {
//		GeneriumStructure s = new GeneriumStructure();
//		s.setOperation("ooooo");
//		s.setTable("eeee");
//		s.setDataKeys(new ArrayList<String>());
//		s.getDataKeys().add("dk1");
//		s.getDataKeys().add("dk2");
//		String str = (new XStream()).toXML(s);
//		System.out.println(str);
		AmanToken token = new AmanToken();
		token.setAppId("1111111");
		token.setBeanId(0);
		token.setId(0);
		token.setNom("nom");
		token.setPassword("password");
		token.setSessionId("SID");
		token.setStatus("status");
		token.setUsername("login");
		
		DataModel m = new DataModel();
		m.setClauses(new ArrayList<SearchClause>());
		m.setDataMap(new ArrayList<DataEntry>());
		m.setEntity("table");
		m.setExpired("");
		m.setNbpages(0);
		m.setOperation("GET");
		m.setPage(0);
		m.setPages(0);
		m.setRows(new ArrayList<DataRow>());
		m.setStatus("");
		m.setToken(token);
		m.setUnrecognized("");
		
		DataRow r = new DataRow();
		r.setDataRow(new ArrayList<DataEntry>());
		DataEntry e = new DataEntry();
		e.setAttributeReference("attribut");
		e.setLabel("libelle");
		e.setList(new ArrayList<DataCouple>());
		e.setType("type");
		e.setValue("");
		DataCouple c = new DataCouple();
		c.setId(1);
		c.setLabel("text");
		e.getList().add(c);
		r.getDataRow().add(e);
		m.getRows().add(r);
		
		m.setClauses(new ArrayList<SearchClause>());
		SearchClause sc = new SearchClause();
		sc.setClause("claus");
		sc.setField("champs");
		sc.setGt("bornemax");
		sc.setLt("bornemax");
		sc.setType("type");
		m.getClauses().add(sc);
		XStream engine = new XStream();
		String value = engine.toXML(m);
		System.out.println(value);
		
	}

}
