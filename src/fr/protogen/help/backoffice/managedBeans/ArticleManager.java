package fr.protogen.help.backoffice.managedBeans;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudItem;
import org.primefaces.model.tagcloud.TagCloudModel;

import fr.protogen.help.backoffice.dao.HelpArticleDAO;
import fr.protogen.help.backoffice.dao.HelpMenuDAO;
import fr.protogen.help.backoffice.dao.HelpQuestionDAO;
import fr.protogen.help.backoffice.dao.HelpTagDAO;
import fr.protogen.help.backoffice.dao.RelationalDAO;
import fr.protogen.help.backoffice.model.HelpArticle;
import fr.protogen.help.backoffice.model.HelpMenu;
import fr.protogen.help.backoffice.model.HelpQuestion;
import fr.protogen.help.backoffice.model.HelpTag;

@ManagedBean
@SessionScoped
public class ArticleManager implements Serializable{

	public static final int AND_SEARCH=1,OR_SEARCH=2;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3890503113692946095L;
	private static final String TAGS_SEPARATOR=",";
	private HelpMenu toAddMenu;
	private List<HelpTag> tagsList;
	private List<HelpQuestion> questionsList;
	private List<HelpMenu> menus ;
	private List<HelpMenu> menuTree;
	private List<HelpArticle> linkedArticles;
	private int articleListSearchType=AND_SEARCH;
	//recherche sémantique
	private boolean autocompletion;

	
	//tags search
	private List<HelpArticle> articlesByTags;
	private String searchTags;
	private boolean resultsFound=false;
	
	//DAO
	private HelpArticleDAO articleDAO;
	private HelpMenuDAO menuDAO;
	private HelpTagDAO tagDAO;
	private RelationalDAO relationalDAO;
	private HelpQuestionDAO questionDAO;
	
	//Edit
	//to avoid article reference in its own edit page
	private List<HelpMenu> editReferencedArticles;

	//Recherche Article list
	private String articleListName="",articleListContent="";
	
	//view attribute
	private String tags="";
	private String questions;
	private String parentMenuId;
	private HelpArticle selectedArticle;
	private List<Long> referencedArticles;
	private int selectedMenuId,selectedChildId;

	//tag cloud
	private TagCloudModel tagModel;
	private boolean hasSearchBeenDone=false;
	List<HelpTag> tagClouditems=null;
	
	//modification
	private HelpMenu toModifyMenu;
	
	private List<HelpMenu> allowedMenus;
	private boolean searchArticleList;
	
	@PostConstruct
	public void initialize() {
		
		boolean notinsession = (!FacesContext.getCurrentInstance()
				.getExternalContext().getSessionMap().containsKey("USER_KEY") || FacesContext
				.getCurrentInstance().getExternalContext().getSessionMap()
				.get("USER_KEY") == null);

		if (notinsession) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		

		//connected
		toAddMenu=new HelpMenu();
		toAddMenu.setArticle(new HelpArticle());
		toAddMenu.setParent(new HelpMenu());
		
		//DAO
		articleDAO=new HelpArticleDAO();
		menuDAO=new HelpMenuDAO();
		tagDAO=new HelpTagDAO();
		relationalDAO=new RelationalDAO();
		questionDAO=new HelpQuestionDAO();
		
		//view
		referencedArticles=new ArrayList<Long>();
		
		//tag cloud
		tagModel=new DefaultTagCloudModel();
		
		//menus=menuDAO.getAllMenus();
		menus=menuDAO.getAllMenus();
		
		//populate menu
		navigateToMenu();
		
		selectedMenuId=-1;
		selectedChildId=-1;
		selectedArticle=new HelpArticle();
	}
	//Resets new article form
	private void resetArticle() {
		toAddMenu=new HelpMenu();
		toAddMenu.setArticle(new HelpArticle());
		tags="";
		questions="";
		parentMenuId="";
		if(referencedArticles!=null){
			referencedArticles.clear();
		}
		
	}
    public void resetArticle(ActionEvent actionEvent){
    	resetArticle();	
	}
	public void persistArticle(ActionEvent actionEvent){
		
		try{
		toAddMenu.getArticle().setLinks(createLinkedArticles(referencedArticles));
		tagsList=tagDAO.saveArticleTags(HelpTag.splitTags(tags));
		questionsList=questionDAO.saveArticleQuestions(HelpQuestion.splitQuestions(questions));
		toAddMenu.setArticle(articleDAO.save(toAddMenu.getArticle()));
		long parentID=Long.parseLong(parentMenuId);
		if(parentID==HelpMenuDAO.NO_PARENT){
			toAddMenu.setParent(null);
		}else{
			HelpMenu parent=new HelpMenu();
			parent.setMenuId(parentID);
			toAddMenu.setParent(parent);
		}
		menuDAO.save(toAddMenu);
		//Associating tags with article
		toAddMenu.getArticle().setTags(tagsList);
		toAddMenu.getArticle().setQuestions(questionsList);
		relationalDAO.saveAllArticleTag(toAddMenu.getArticle());
		relationalDAO.saveAllArticleLink(toAddMenu.getArticle());
		relationalDAO.saveAllArticleQuestion(toAddMenu.getArticle());
		
		menus.add(toAddMenu);
		resetArticle();
		 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info: ", "L'article est enregistré avec succès"));
		//update menu tree in case articles page is shown next
		navigateToMenu();
		}catch(Exception e){
			e.printStackTrace();
			 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur: ", "L'article n'a pas pu être enregistré"));
	      
		}
	}
	

	
public String editArticle(){
		boolean hasParentChanged=false;
		try{
		toModifyMenu.getArticle().setLinks(createLinkedArticles(referencedArticles));
		tagsList=tagDAO.saveArticleTags(HelpTag.splitTags(tags));
		questionsList=questionDAO.saveArticleQuestions(HelpQuestion.splitQuestions(questions));
		articleDAO.modify(toModifyMenu.getArticle());
		
		long parentID=Long.parseLong(parentMenuId);
		
		if((toModifyMenu.getParent()==null && parentID !=-1) || 
				(toModifyMenu.getParent()!=null && toModifyMenu.getParent().getMenuId()!=parentID)){
			hasParentChanged=true;
		}
		
		if(parentID==HelpMenuDAO.NO_PARENT){
			toModifyMenu.setParent(null);
		}else{
			HelpMenu parent=new HelpMenu();
			parent.setMenuId(parentID);
			toModifyMenu.setParent(parent);
		}
		menuDAO.modify(toModifyMenu);
		
		//Associating tags with article
		toModifyMenu.getArticle().setTags(tagsList);
		toModifyMenu.getArticle().setQuestions(questionsList);
		
		relationalDAO.modifyAllArticleTag(toModifyMenu.getArticle());
		relationalDAO.modifyAllArticleLink(toModifyMenu.getArticle());
		relationalDAO.modifyAllArticleQuestion(toModifyMenu.getArticle());
		
		//Removing non attached question / tags
		questionDAO.removeUnlinked();
		tagDAO.removeUnlinked();
		//if menu parent has changed
		//reload menus
		if(hasParentChanged){
			menus=menuDAO.getAllMenus();
		}
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info: ", "L'article est modifié avec succès"));
		
		toModifyMenu=null;
		initializeCreationMenu();
		return "wiki-ArticleList.xhtml";
		}catch(Exception e){
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur: ", "L'article n'a pas pu être modifié"));
			return ""; 
		}
	}

	public String cancelEditArticle(){
		resetArticle();
		toModifyMenu=null;
		return "wiki-ArticleList.xhtml";
	}
	public void deleteArticle(ActionEvent actionEvent){
		HelpMenu menuToDelete = (HelpMenu)actionEvent.getComponent().getAttributes().get("menuToDelete");
		
		if(menuDAO.deleteMenu(menuToDelete)){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info: ", "L'article "+menuToDelete.getArticle().getTitle()+" est supprimé avec succès"));
			menus.remove(menuToDelete);
			//update menu tree in case articles page is shown next
			navigateToMenu();
		}else{
			 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur: ", "L'article "+menuToDelete.getArticle().getTitle()+"  n'a pas pu être supprimé"));
		}
		
	}
	
	public void modifyArticle(ActionEvent actionEvent){
		toModifyMenu = (HelpMenu)actionEvent.getComponent().getAttributes().get("menuToModify");
		tags=HelpTag.formatTags(toModifyMenu.getArticle().formateTags()).trim();
		parentMenuId=String.valueOf(toModifyMenu.getParent()==null?-1:toModifyMenu.getParent().getMenuId());
		questions=toModifyMenu.getArticle().formatQuestions();
		referencedArticles.clear();
		for(HelpArticle article:toModifyMenu.getArticle().getLinks()){
			referencedArticles.add(article.getArticleId());
		}
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("wiki-ModifyArticle.xhtml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String navigateToMenu(){
		menuTree= menuDAO.getMenuTree();
		if(menuTree!=null && menuTree.size()>0){
			
			String sid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("MENUINDEX");
			String linkedArticleIDParam=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("LinkedArticleID");
			//linked articles navigation
			if(linkedArticleIDParam!=null){
				int linkArticleId=Integer.parseInt(linkedArticleIDParam);
				int menuIndex=0;
				for(HelpMenu menu: menuTree){
					if(menu.getArticle().getArticleId()==linkArticleId){
						selectedArticle=menu.getArticle();
						selectedMenuId=menuIndex;
						selectedChildId=-1;
						break;
					}
					else if(menu.getChilds()!=null && menu.getChilds().size()>0){
						int childMenuIndex=0;
						for(HelpMenu childMenu:menu.getChilds()){
							if(childMenu.getArticle().getArticleId()==linkArticleId){
								selectedArticle=childMenu.getArticle();
								selectedMenuId=menuIndex;
								selectedChildId=childMenuIndex;
								return "wiki-articles.xhtml";
							}
							childMenuIndex++;
						}
					}
					menuIndex++;
				}
			}
			//menu navigation
			else {
				if(sid==null) {
					selectedArticle=menuTree.get(0).getArticle();
					selectedMenuId=0;
					selectedChildId=-1;
				}else{
					
					// child menu
					// index 1 : parent id
					// index 2 : child id
					if(sid.contains("-")){
						String[] ids=sid.split("-");
						selectedMenuId=Integer.parseInt(ids[0]);
						selectedChildId=Integer.parseInt(ids[1]);
						selectedArticle=menuTree.get(selectedMenuId).getChilds().get(selectedChildId).getArticle();
						
					}
					//parent menu
					else{
						selectedMenuId=Integer.parseInt(sid);
						selectedChildId=-1;
						selectedArticle=menuTree.get(selectedMenuId).getArticle();
					}
					
				}
			}

			//getting tag cloud item
			populateTagCloud();
		}

		
		return "wiki-articles.xhtml";
	}
	
	public void onTagItemSelect(SelectEvent event) {
	     autocompletion=false;
	     TagCloudItem item = (TagCloudItem) event.getObject();
	     searchTags=item.getLabel();
	     try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(searchByTags());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String resetSearchArticleList(){
		articleListName="";
		articleListContent="";
		searchArticleList=false;
		
		return "";
	}
	//populate tag cloud items
	private void populateTagCloud() {
		//a search has been done -> update tag cloud
		//first time to show the tag -> create tag cloud
		if(hasSearchBeenDone || tagClouditems==null){
			hasSearchBeenDone=false;
			tagClouditems=tagDAO.getTagsForCloud();
			if(tagClouditems!=null && tagClouditems.size()>0){
				tagModel.clear();
				for(HelpTag tag:tagClouditems){
					tagModel.addTag(new DefaultTagCloudItem(tag.getLabel(),calculateStrength(tag.getQueries())));
				}
			}
		}

	}
	//calculate the tag cloud item  strength based on queries which involves the tag
	private int calculateStrength(long queries) {
		if(queries <1000){
			return 1;
		}else if(queries >=1000 && queries < 2000){
			return 2;
		}else if(queries >=2000 && queries < 3000){
			return 3;
		}else if(queries >=3000 && queries < 4000 ){
			return 4;
		}else if(queries >=4000){
			return 5;
		}else{
			return 1;
		}
	
	}
	public String searchByTags(){
		if(articlesByTags!=null){
			articlesByTags.clear();
		}
		if(!searchTags.isEmpty()){
			//Question mode search
			if(autocompletion){
				if(!searchTags.equalsIgnoreCase("aucune proposition")){
					articlesByTags=articleDAO.getArticlesByQuestions(searchTags);
				}
				
			}
			//Tag mode search
			else{
				articlesByTags=articleDAO.getArticlesByTags(searchTags);
				String[] searchFormattedTags=HelpTag.splitTags(searchTags);
				searchTags="";
				for(String tag:searchFormattedTags){
					searchTags+=(searchTags.isEmpty()?tag:" "+tag);
				}
			}
			
		}

		if(articlesByTags!=null && articlesByTags.size()>0){
			hasSearchBeenDone=true;
			resultsFound=true;
		}else{
			hasSearchBeenDone=false;
			resultsFound=false;
		}
		selectedMenuId=-1;
		selectedChildId=-1;
		return "wiki-searchResults.xhtml";
	}
	
	public String searchArticleList(){
		if(!articleListName.isEmpty() || !articleListContent.isEmpty()){
			searchArticleList=true;
		}else{
			searchArticleList=false;
		}
		return "";
	}
	
	//autocompletion
	public List<String> autoCompleteQuestion(String query) {
        List<String> results = new ArrayList<String>();
        //autocompeltion mode
        if(autocompletion){
	        results=questionDAO.getSuggestions(query);
	        if(results.size()==0){
	        	results.add("Aucune proposition");
	        }
        }
        return results;
    }

	
	private List<HelpArticle> createLinkedArticles(
			List<Long> referencedArticles) {
		linkedArticles=new ArrayList<HelpArticle>();
		HelpArticle article;
		for(Long articleId:referencedArticles){
			article=new HelpArticle();
			article.setArticleId(articleId);
			linkedArticles.add(article);

		}
		
		return linkedArticles;
	}

	//navigation to Article creation page
	public String toNewArticlePage(){
		initializeCreationMenu();
		return "wiki-AddArticle.xhtml";
	}
	//initialize parent and referenced articles menus
	private void initializeCreationMenu() {
		parentMenuId=String.valueOf(-1);
		referencedArticles.clear();
		tags="";
		questions="";
	
	}
	
	public HelpMenu getToAddMenu() {
		return toAddMenu;
	}
	public void setToAddMenu(HelpMenu toAddMenu) {
		this.toAddMenu = toAddMenu;
	}
	public List<HelpMenu> getMenus() {
		
		if(searchArticleList){
			List<HelpMenu> resultMenus=new ArrayList<HelpMenu>();
			if(articleListSearchType==ArticleManager.OR_SEARCH){
				if(!articleListName.isEmpty()){
					for(HelpMenu menu:menus){
						if(menu.getArticle().getTitle().toLowerCase().contains(articleListName.toLowerCase())){
							resultMenus.add(menu);
						}
					}
				}
				if(!articleListContent.isEmpty()){
					for(HelpMenu menu:menus){
						if(menu.getArticle().getContent().toLowerCase().contains(articleListContent.toLowerCase()) && !resultMenus.contains(menu)){
							resultMenus.add(menu);
						}
					}
				}
			}
			else{
				if(!articleListName.isEmpty() && !articleListContent.isEmpty()){
					for(HelpMenu menu:menus){
						if(menu.getArticle().getTitle().toLowerCase().contains(articleListName.toLowerCase())
								&& menu.getArticle().getContent().toLowerCase().contains(articleListContent.toLowerCase())){
							resultMenus.add(menu);
						}
					}
				}
			}
			
			
			return resultMenus;
		}
		else{
			return menus;
		}
		
	}
	public void setMenus(List<HelpMenu> menus) {
		this.menus = menus;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags=tags;
	}

	
	public String getQuestions() {
		return questions;
	}
	
	public boolean isAutocompletion() {
		return autocompletion;
	}
	public void setAutocompletion(boolean autocompletion) {
		this.autocompletion = autocompletion;
	}
	
	public void setQuestions(String questions) {
		this.questions = questions;
	}
	public String getParentMenuId() {
		return parentMenuId;
	}

	public void setParentMenuId(String parentMenuId) {
		this.parentMenuId = parentMenuId;
	}

	public HelpArticle getSelectedArticle() {
		return selectedArticle;
	}

	public void setSelectedArticle(HelpArticle selectedArticle) {
		this.selectedArticle = selectedArticle;
	}

	public List<HelpMenu> getMenuTree() {
		
		return menuTree;
	}

	public void setMenuTree(List<HelpMenu> menuTree) {
		this.menuTree = menuTree;
	}

	

	public List<Long> getReferencedArticles() {
		return referencedArticles;
	}

	public void setReferencedArticles(List<Long> referencedArticles) {
		this.referencedArticles = referencedArticles;
	}

	public int getSelectedMenuId() {
		return selectedMenuId;
	}

	public void setSelectedMenuId(int selectedMenuId) {
		this.selectedMenuId = selectedMenuId;
	}

	public int getSelectedChildId() {
		return selectedChildId;
	}

	public void setSelectedChildId(int selectedChildId) {
		this.selectedChildId = selectedChildId;
	}
	public List<HelpArticle> getArticlesByTags() {
		return articlesByTags;
	}
	public void setArticlesByTags(List<HelpArticle> articlesByTags) {
		this.articlesByTags = articlesByTags;
	}
	public String getSearchTags() {
		return searchTags;
	}
	public void setSearchTags(String searchTags) {
		this.searchTags = searchTags;
	}
	public boolean isResultsFound() {
		return resultsFound;
	}
	public void setResultsFound(boolean resultsFound) {
		this.resultsFound = resultsFound;
	}
	public TagCloudModel getTagModel() {
		return tagModel;
	}
	public void setTagModel(TagCloudModel tagModel) {
		this.tagModel = tagModel;
	}
	public HelpMenu getToModifyMenu() {
		return toModifyMenu;
	}
	public void setToModifyMenu(HelpMenu toModifyMenu) {
		this.toModifyMenu = toModifyMenu;
	}
	
	public List<HelpMenu> getEditReferencedArticles() {
		return editReferencedArticles;
	}
	public void setEditReferencedArticles(List<HelpMenu> editReferencedArticles) {
		this.editReferencedArticles = editReferencedArticles;
	}
	public List<HelpMenu> getAllowedMenus() {
		allowedMenus=new ArrayList<HelpMenu>();
		editReferencedArticles=new ArrayList<HelpMenu>();
		
		
		if(toModifyMenu!=null){
			editReferencedArticles.addAll(menus);
			//remove the edited menu from both parent and article referenced menus
			editReferencedArticles.remove(toModifyMenu);
		}
		//remove the edited article's childs from parent menu (to avoid that a child menu is the parent menu of its parent menu)
	    if(toModifyMenu!=null && toModifyMenu.getChilds()!=null && toModifyMenu.getChilds().size()>0){
	    	return allowedMenus;
	    }
		for(HelpMenu menu:menus){
			//remove menus that are child menus and self
			//2 level allowed
			if(menu.getParent()==null){
				if(toModifyMenu==null ||(toModifyMenu!=null && toModifyMenu.getMenuId()!=menu.getMenuId())){
					allowedMenus.add(menu);
				}
				
			}
		}
			
	    return allowedMenus;

	}
	public void setAllowedMenus(List<HelpMenu> allowedMenus) {
		this.allowedMenus = allowedMenus;
	}
	public String getArticleListName() {
		return articleListName;
	}
	public void setArticleListName(String articleListName) {
		this.articleListName = articleListName;
	}
	public String getArticleListContent() {
		return articleListContent;
	}
	public void setArticleListContent(String articleListContent) {
		this.articleListContent = articleListContent;
	}
	public int getArticleListSearchType() {
		return articleListSearchType;
	}
	public void setArticleListSearchType(int articleListSearchType) {
		this.articleListSearchType = articleListSearchType;
	}
	
	
	
	
	

	
	
	
	
	
}
