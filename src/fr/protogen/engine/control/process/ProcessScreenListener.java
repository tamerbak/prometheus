package fr.protogen.engine.control.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;


import fr.protogen.engine.utils.ProtogenConstants;
import fr.protogen.masterdata.model.CUIParameter;
import fr.protogen.masterdata.model.CoreUser;
import fr.protogen.masterdata.model.SAtom;
import fr.protogen.masterdata.model.SProcedure;
import fr.protogen.masterdata.model.SStep;

public class ProcessScreenListener {
	private static ProcessScreenListener instance = null;
	private List<ProcessSession> sessions;
	private String formview="";
	
	public synchronized static ProcessScreenListener getInstance(){
		if(instance == null)
			instance = new ProcessScreenListener();
		
		return instance;
	}
	private ProcessScreenListener(){
		setSessions(new ArrayList<ProcessSession>());
		formview = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("FORM_MODE").toString();
	}
	
	public List<ProcessSession> getSessions() {
		return sessions;
	}
	public void setSessions(List<ProcessSession> sessions) {
		this.sessions = sessions;
	}
	public String getNextScreen(CoreUser user, List<CUIParameter> uiParameters,
			List<Map<String, String>> data) {
		// TODO Auto-generated method stub
		
		boolean finished = false;
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				if(data!=null)
					s.setData(data);
				
				SAtom nextW = null;
				for(SAtom w : s.getProcedure().getAtoms()){
					if(w.getId() == s.getCurrentWindow().getId()){
						if(s.getProcedure().getAtoms().indexOf(w) == (s.getProcedure().getAtoms().size()-1)){
							finished = true;
							break;
						}
						if(w.getType().getId()==2)
						{
							for(CUIParameter p : uiParameters){
								s.getParameters().put(p.getParameterKey(), p.getValue());
							}
						}	
						nextW =  s.getProcedure().getAtoms().get(s.getProcedure().getAtoms().indexOf(w)+1);
					}
				}
				
				if(!finished){
					s.setCurrentWindow(nextW);
					if(nextW.getType().getId()==2)
					{
						FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.PARAMETER_ATOM, nextW);
						return "protogen-parameters";
					}
					if(nextW.getType().getId() == 1 && nextW.getWindow().getCWindowtype().getId() == 1)
						return "protogen-listview";
					else if(nextW.getType().getId() == 1 && nextW.getWindow().getCWindowtype().getId() == 2){
						return formview;
					} else if(nextW.getType().getId()==3) {
						FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.RESOURCE_INOUT, nextW.getResource());
						return "protogen-resin";
					} else if(nextW.getType().getId()==4) {
						FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.RESOURCE_INOUT, nextW.getResource());
						return "protogen-resout";
					}  else if(nextW.getType().getId()==5) {
						FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
						return "protogen-sendmail";
					} else if(nextW.getType().getId()==6) {
						FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
						return "protogen-sendsms";
					}else if(nextW.getType().getId()==8) {
						FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
						return "protogen-sendappel";
					}
					else if(nextW.getType().getId()==7) {
						FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
						return "protogen-sendfax";
					} 
					else if(nextW.getType().getId()>7) {
						FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
						return "protogen-communication";
					}

				} else {
					return "protogen-synthesis";
				}
				
				break;
			}
		}
		
		return "protogen";
	}
	
	public String getSpecificScreen(CoreUser user, int atomId) {
		// TODO Auto-generated method stub
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
								
				SAtom nextW = new SAtom();
				for(SAtom w : s.getProcedure().getAtoms()){
						if(w.getId() == atomId)
							nextW =  w;
				}
				
				
				s.setCurrentWindow(nextW);
				if(nextW.getType().getId()==2)
				{
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.PARAMETER_ATOM, nextW);
					return "protogen-parameters";
				}
				if(nextW.getType().getId() == 1 && nextW.getWindow().getCWindowtype().getId() == 1)
					return "protogen-listview";
				else if(nextW.getType().getId() == 1 && nextW.getWindow().getCWindowtype().getId() == 2){
					return formview;
				} else if(nextW.getType().getId()==3) {
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.RESOURCE_INOUT, nextW.getResource());
					return "protogen-resin";
				} else if(nextW.getType().getId()==4) {
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.RESOURCE_INOUT, nextW.getResource());
					return "protogen-resout";
				}   else if(nextW.getType().getId()==5) {
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
					return "protogen-sendmail";
				} else if(nextW.getType().getId()==6) {
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
					return "protogen-sendsms";
				}else if(nextW.getType().getId()==8) {
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
					return "protogen-sendappel";
				}
				else if(nextW.getType().getId()==7) {
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
					return "protogen-sendfax";
				}  else if(nextW.getType().getId()>7) {
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, nextW.getCommunication());
					return "protogen-communication";
				}
				
				break;
			}
		}
		
		return "protogen";
	}
	
	public SAtom getCurrentScreen(CoreUser user) {
		// TODO Auto-generated method stub
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				return s.getCurrentWindow();
			}
		}
		
		return null;
	}
	public String createNewProcess(CoreUser user, SProcedure process, ProcessEvoltionListener listener) {
		// TODO Auto-generated method stub
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId())
				sessions.remove(s);
		}
		
		ProcessSession s = new ProcessSession();
		s.setListener(listener);
		s.setCurrentUser(user);
		s.setParameters(new HashMap<String, String>());
		s.setData(new ArrayList<Map<String,String>>());
		s.setProcedure(process);
		
		//	process.organizeAtoms();
		//	DEBUT
		process.setAtoms(new ArrayList<SAtom>());
		for(SStep et : process.getEtapes()){
			
			for(SAtom a : et.getActions())
				a.setStep(et);
			
			process.getAtoms().addAll(et.getActions());
		}
		
		//	FIN
		
		SAtom window = process.getAtoms().get(0);
		s.setCurrentWindow(window);
		
		sessions.add(s);
		
		if(window.getType().getId()==2)
		{
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.PARAMETER_ATOM, window);
			return "protogen-parameters";
		}
		if(window.getType().getId() == 1 && window.getWindow().getCWindowtype().getId() == 1)
			return "protogen-listview";
		else if(window.getType().getId() == 1 && window.getWindow().getCWindowtype().getId() == 2){
			return formview;
		} else if(window.getType().getId()==3) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.RESOURCE_INOUT, window.getResource());
			return "protogen-resin";
		} else if(window.getType().getId()==4) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.RESOURCE_INOUT, window.getResource());
			return "protogen-resout";
		}   else if(window.getType().getId()==5) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, window.getCommunication());
			return "protogen-sendmail";
		} else if(window.getType().getId()==6) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, window.getCommunication());
			return "protogen-sendsms";
		}else if(window.getType().getId()==7) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, window.getCommunication());
			return "protogen-sendfax";
		}else if(window.getType().getId()==8) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, window.getCommunication());
			return "protogen-sendappel";
		}
		else if(window.getType().getId()>7) {
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ProtogenConstants.COMM_ATOM, window.getCommunication());
			return "protogen-communication";
		}
		
		return "protogen";
	}
	public void destroyCurrentScreen(CoreUser user) {
		// TODO Auto-generated method stub
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				sessions.remove(s);
				break;
			}
		}
		
	}
	public Map<String, String> getParameters(CoreUser user) {
		// TODO Auto-generated method stub
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				return s.getParameters();
			}
		}
		return null;
	}
	
	public void kill(CoreUser user) {
		// TODO Auto-generated method stub
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				sessions.remove(s);
				s=null;
				break;
			}
		}
	}
	public ProcessEvoltionListener getSessionListener(CoreUser user){
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				return s.getListener();
			}
		}
		
		return null;
	}
	public String getNextScreenLabel(CoreUser user) {
		// TODO Auto-generated method stub
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				String StepTitle = s.getCurrentWindow().getStep().getTitle();
				for(SStep e : s.getProcedure().getEtapes()){
					if(e.getTitle() == StepTitle){
						if(s.getProcedure().getEtapes().indexOf(e) == (s.getProcedure().getEtapes().size()-1))
								return "";
						return s.getProcedure().getEtapes().get(s.getProcedure().getEtapes().indexOf(e)+1).getTitle();
					}
				}
			}
		}
			
				
		return "";
	}
	public List<SAtom> getNextAtoms(CoreUser user) {
		// TODO Auto-generated method stub
		List<SAtom> atoms = new ArrayList<SAtom>(); 
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				SStep etape = s.getCurrentWindow().getStep();
				for(int i = 0 ; i < s.getProcedure().getEtapes().size();i++){
					SStep e = s.getProcedure().getEtapes().get(i);
					if(e.getId()==etape.getId()){
						if(i<s.getProcedure().getEtapes().size()-1)
							return s.getProcedure().getEtapes().get(i+1).getActions();
					}
				}
			}
		}
		return atoms;
	}
	public List<SAtom> getPreviousAtoms(CoreUser user) {
		// TODO Auto-generated method stub
		List<SAtom> atoms = new ArrayList<SAtom>(); 
		for(ProcessSession s : sessions){
			if(s.getCurrentUser().getId() == user.getId()){
				SStep etape = s.getCurrentWindow().getStep();
				for(int i = 0 ; i < s.getProcedure().getEtapes().size();i++){
					SStep e = s.getProcedure().getEtapes().get(i);
					if(e.getId()==etape.getId()){
						if(i>0)
							return s.getProcedure().getEtapes().get(i-1).getActions();
					}
				}
			}
		}
		return atoms;
	}
	
}
