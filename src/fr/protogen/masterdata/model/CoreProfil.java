package fr.protogen.masterdata.model;

import java.util.Date;
import java.util.List;

/*
 * THIS IS ROLES
 */
public class CoreProfil implements java.io.Serializable {

		private static final long serialVersionUID = 1373600538444111111L;
		private int id;
		private String code;
		private String libelle;
		private Date dateEffet;
		private Date dateFin;
		private CoreRole role;
		private List<CoreDataConstraint> constraints;
		private String description;

		public CoreProfil() {
		}

		public CoreProfil(int id, String code, String libelle, Date date_effet, Date date_fin, CoreRole role) {
			this.id = id;
			this.role = role;
			this.code = code;
			this.dateEffet = date_effet;
			this.dateFin = date_fin;
			this.libelle = libelle;
		}

		public int getId() {
			return this.id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getLibelle() {
			return libelle;
		}

		public void setLibelle(String libelle) {
			this.libelle = libelle;
		}

		public Date getDateEffet() {
			return dateEffet;
		}

		public void setDateEffet(Date date_effet) {
			this.dateEffet = date_effet;
		}

		public Date getDateFin() {
			return dateFin;
		}

		public void setDateFin(Date date_fin) {
			this.dateFin = date_fin;
		}

		public CoreRole getRole() {
			return role;
		}

		public void setRole(CoreRole role) {
			this.role = role;
		}

		public List<CoreDataConstraint> getConstraints() {
			return constraints;
		}

		public void setConstraints(List<CoreDataConstraint> constraints) {
			this.constraints = constraints;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}