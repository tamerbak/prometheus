-- Table: core_user_role

-- DROP TABLE core_user_role;

CREATE TABLE core_user_role
(
  id_user integer NOT NULL,
  id_role integer NOT NULL,
  CONSTRAINT user_role_prim PRIMARY KEY (id_user, id_role)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE core_user_role
  OWNER TO jakj;


--	DB UPDATE
--	DATE	11 JUL 2014
--	MINI PROJECT	CLASS EDITOR
alter table s_application
	add column json_digram text;


--	DB UPDATE
--	DATE	12 JUL 2014
--	MINI PROJECT	BLOB RESOURCES
CREATE SEQUENCE public.s_resource_table_seq;
ALTER SEQUENCE public.s_resource_table_seq
  OWNER TO jakj;

CREATE TABLE s_resource_table
(
  id integer NOT NULL DEFAULT nextval('s_resource_table_seq'::regclass),
  key character varying(512) NOT NULL,
  filename character varying(512) NOT NULL,
  content bytea,
  CONSTRAINT s_resource_table_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE s_resource_table
  OWNER TO jakj;

ALTER TABLE core_role ADD COLUMN 
	logo character varying(512);

--  DATA UPDATE
--  DATE 12 JUL 2014
--  MINI PROJECT PROFILE PICTURE

ALTER TABLE core_user
  ADD COLUMN picto character varying(512);
ALTER TABLE core_user
  DROP CONSTRAINT fk_core_user_core_role;
ALTER TABLE core_user
  ADD CONSTRAINT fk_core_user_core_role FOREIGN KEY ("idRole")
      REFERENCES core_role (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

--  DATA UPDATE
--  DATE 12 JUILLET 2014
--  MINI PROJECT ROLE DESCRIPTION
ALTER TABLE core_role
  ADD COLUMN description text;

--  DATA UPDATE
--  DATE 13 JUL 2014
--  MINI PROJET CONTEXT PERSISTENCE
CREATE TABLE c_temporary_form_data
(
  id_user integer NOT NULL,
  id_form integer NOT NULL,
  form_data text,
  CONSTRAINT pk_temporary_form_data PRIMARY KEY (id_user),
  CONSTRAINT fk_temp_user FOREIGN KEY (id_user)
      REFERENCES core_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_temp_form FOREIGN KEY (id_form)
      REFERENCES c_window (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_temporary_form_data
  OWNER TO jakj;

--  DATA UPDATE
--  DATE 14 JUL 2014
--  MINI PROJET DATA CONTRAINT ON ROLES
CREATE TABLE public.core_data_access_right
(
   role integer NOT NULL, 
   entity integer NOT NULL, 
   data_value integer, 
   PRIMARY KEY (role, entity), 
   FOREIGN KEY (role) REFERENCES core_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (entity) REFERENCES c_businessclass (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.core_data_access_right
  OWNER TO jakj;

--  DATA UPDATE
--  DATE 14 JUL 2014
--  MINI PROJET EVENTS AND CALENDAR
CREATE SEQUENCE public.e_event_seq;
ALTER SEQUENCE public.e_event_seq
  OWNER TO jakj;

CREATE SEQUENCE public.e_event_instance_seq;
ALTER SEQUENCE public.e_event_instance_seq
  OWNER TO jakj;

CREATE TABLE public.e_event
(
   id integer NOT NULL DEFAULT nextval('e_event_seq'::regclass), 
   titre character varying(128), 
   type integer, 
   contenu text, 
   destinataire integer, 
   PRIMARY KEY (id), 
   FOREIGN KEY (destinataire) REFERENCES core_user (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.e_event
  OWNER TO jakj;
CREATE TABLE public.e_event_instance
(
   id integer NOT NULL DEFAULT nextval('e_event_instance_seq'::regclass), 
   event integer, 
   creation timestamp with time zone, 
   consultation timestamp with time zone, 
   content text, 
   state character(1) NOT NULL DEFAULT 'N', 
   PRIMARY KEY (id), 
   FOREIGN KEY (event) REFERENCES e_event (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.e_event_instance
  OWNER TO jakj;

ALTER TABLE e_event 
  ADD COLUMN entite integer;

ALTER TABLE e_event 
  ADD COLUMN operation integer;

ALTER TABLE e_event
  ADD COLUMN bean integer;

--  DATA UPDATE
--  DATE 14 JUL 2014
--  MINI PROJET CLASS DIAGRAM
ALTER TABLE s_application
  ADD COLUMN json_diagram text;

--  DATA UPDATE
--  DATE 14 JUL 2014
--  MINI PROJET POST EVENT ACTIONS
CREATE SEQUENCE public.e_postevent_action_seq;
ALTER SEQUENCE public.e_postevent_action_seq
  OWNER TO jakj;

CREATE SEQUENCE public.e_pea_window_seq;
ALTER SEQUENCE public.e_pea_window_seq
  OWNER TO jakj;

CREATE TABLE public.e_postevent_action
(
   id integer NOT NULL DEFAULT nextval('e_postevent_action_seq'::regclass), 
   event integer, 
   type integer,
   PRIMARY KEY (id), 
   FOREIGN KEY (event) REFERENCES e_event (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.e_postevent_action
  OWNER TO jakj;

CREATE TABLE public.e_pea_window
(
  id integer NOT NULL DEFAULT nextval('e_pea_window_seq'::regclass), 
  action integer NOT NULL,
  screen integer NOT NULL,
  details character(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (id),
  FOREIGN KEY (action) REFERENCES e_postevent_action (id) ON UPDATE NO ACTION ON DELETE NO ACTION,
  FOREIGN KEY (screen) REFERENCES c_window (id) ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.e_pea_window
  OWNER TO jakj;

ALTER TABLE public.e_event_instance
  ADD COLUMN row_id integer;

--  DATA UPDATE
--  DATE 16 JUL 2014
--  MINI PROJET POST EVENT COMM ACTIONS
CREATE SEQUENCE public.e_pea_sms_seq;
ALTER SEQUENCE public.e_pea_sms_seq
  OWNER TO jakj;

CREATE TABLE e_pea_sms
(
  id integer NOT NULL DEFAULT nextval('e_pea_sms_seq'::regclass),
  action integer NOT NULL,
  sms_subject character varying(128),
  sms_text_pattern text,
  CONSTRAINT e_pea_sms_pkey PRIMARY KEY (id),
  CONSTRAINT e_pea_sms_action_fkey FOREIGN KEY (action)
      REFERENCES e_postevent_action (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE e_pea_sms
  OWNER TO jakj;

CREATE SEQUENCE public.e_pea_email_seq;
ALTER SEQUENCE public.e_pea_email_seq
  OWNER TO jakj;

CREATE TABLE e_pea_email
(
  id integer NOT NULL DEFAULT nextval('e_pea_email_seq'::regclass),
  action integer NOT NULL,
  email_subject character varying(128),
  email_text_pattern text,
  CONSTRAINT e_pea_email_pkey PRIMARY KEY (id),
  CONSTRAINT e_pea_email_action_fkey FOREIGN KEY (action)
      REFERENCES e_postevent_action (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE e_pea_email
  OWNER TO jakj;

--  DATA UPDATE
--  DATE 16 JUL 2014
--  MINI PROJET DATA HISTORY
CREATE SEQUENCE public.c_data_history_seq;
ALTER SEQUENCE public.c_data_history_seq
  OWNER TO jakj;

CREATE TABLE public.c_data_history
(
  id integer NOT NULL DEFAULT nextval('c_data_history_seq'::regclass),
  entity integer NOT NULL,
  attribute integer NOT NULL,
  CONSTRAINT c_data_history_pkey PRIMARY KEY (id),
  CONSTRAINT c_attribute_fkey FOREIGN KEY (attribute)
      REFERENCES c_attribute (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)

WITH (
  OIDS=FALSE
);
ALTER TABLE c_data_history
  OWNER TO jakj;

CREATE SEQUENCE public.c_instance_history_seq;
ALTER SEQUENCE public.c_instance_history_seq
  OWNER TO jakj;

CREATE TABLE public.c_instance_history
(
  id integer NOT NULL DEFAULT nextval('c_instance_history_seq'::regclass),
  history integer NOT NULL,
  bean integer NOT NULL,
  date_debut timestamp with time zone,
  date_fin timestamp with time zone,
  courant character(1) NOT NULL DEFAULT 'N',
  CONSTRAINT c_instance_history_pkey PRIMARY KEY (id),
  CONSTRAINT c_data_history_fkey FOREIGN KEY (history)
      REFERENCES c_data_history (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_instance_history
  OWNER TO jakj;

ALTER TABLE c_instance_history
  ADD COLUMN attribute_id integer;

--  DATA UPDATE
--  DATE 18 JUL 2014
--  MINI PROJET COMPOSITE DATA STRUCTURE
CREATE SEQUENCE public.c_composition_seq;
ALTER SEQUENCE public.c_composition_seq
  OWNER TO jakj;
CREATE TABLE public.c_composition
(
  id integer NOT NULL DEFAULT nextval('c_composition_seq'::regclass),
  entity integer NOT NULL,
  CONSTRAINT pkey_c_composition PRIMARY KEY (id),
  CONSTRAINT fkey_c_composition_seq_c_businessclass FOREIGN KEY (entity)
    REFERENCES c_businessclass (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_composition_rule
  OWNER TO jakj;

CREATE SEQUENCE public.c_composition_rule_seq;
ALTER SEQUENCE public.c_composition_rule_seq
  OWNER TO jakj;

CREATE TABLE public.c_composition_rule
(
  id integer NOT NULL DEFAULT nextval('c_composition_rule_seq'::regclass),
  attribute integer NOT NULL,
  composition integer NOT NULL,
  criterion text,
  CONSTRAINT pkey_c_composition_rule PRIMARY KEY (id),
  CONSTRAINT fkey_c_composition_rule_seq_c_attribute FOREIGN KEY (attribute)
    REFERENCES c_attribute (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkey_c_composition_rule_c_composition FOREIGN KEY (composition)
    REFERENCES c_composition (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_composition_rule
  OWNER TO jakj;

CREATE SEQUENCE public.c_composed_bean_seq;
ALTER SEQUENCE public.c_composed_bean_seq
  OWNER TO jakj;

CREATE TABLE public.c_composed_bean
(
  id integer NOT NULL DEFAULT nextval('c_composed_bean_seq'::regclass),
  bean integer NOT NULL,
  composition integer NOT NULL,
  CONSTRAINT pkey_c_composedbean PRIMARY KEY (id),
  CONSTRAINT fkey_c_composed_bean_composition FOREIGN KEY (composition)
    REFERENCES c_composition (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_composed_bean
  OWNER TO jakj;

CREATE TABLE public.c_composing_bean
(
  composed integer NOT NULL,
  bean integer NOT NULL,
  CONSTRAINT pkey_c_composing_bean PRIMARY KEY (bean, composed),
  CONSTRAINT fkey_c_composing_bean_composed_bean FOREIGN KEY (composed)
    REFERENCES c_composed_bean (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_composing_bean
  OWNER TO jakj;

--  DATA UPDATE
--  DATE 18 JUL 2014
--  MINI PROJET WORKFLOW ENGINE

-- Sequence: s_wf_choice_node_seq

-- DROP SEQUENCE s_wf_choice_node_seq;

CREATE SEQUENCE s_wf_choice_node_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE s_wf_choice_node_seq
  OWNER TO jakj;

-- Table: s_wf_choice_node

-- DROP TABLE s_wf_choice_node;

CREATE TABLE s_wf_choice_node
(
  node integer,
  id integer NOT NULL DEFAULT nextval('s_wf_choice_node_seq'::regclass),
  CONSTRAINT s_wf_choice_node_pkey PRIMARY KEY (id),
  CONSTRAINT s_wf_choice_node_node_fkey FOREIGN KEY (node)
      REFERENCES s_wf_node (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE s_wf_choice_node
  OWNER TO jakj;


-- Sequence: s_wf_choice_seq

-- DROP SEQUENCE s_wf_choice_seq;

CREATE SEQUENCE s_wf_choice_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 7
  CACHE 1;
ALTER TABLE s_wf_choice_seq
  OWNER TO jakj;

-- Table: s_wf_choice

-- DROP TABLE s_wf_choice;

CREATE TABLE s_wf_choice
(
  node integer,
  id integer NOT NULL DEFAULT nextval('s_wf_choice_seq'::regclass),
  transition integer,
  ref_node integer,
  title character varying(64),
  CONSTRAINT s_wf_choice_pkey PRIMARY KEY (id),
  CONSTRAINT s_wf_choice_node_fkey FOREIGN KEY (node)
      REFERENCES s_wf_node (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT s_wf_choice_ref_node_fkey FOREIGN KEY (ref_node)
      REFERENCES s_wf_node (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT s_wf_choice_transition_fkey FOREIGN KEY (transition)
      REFERENCES s_wf_transition (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE s_wf_choice
  OWNER TO jakj;

ALTER TABLE public.s_wf_definition
  ADD COLUMN json_diagram text;

--  DATA UPDATE
--  DATE 19 JUL 2014
--  MINI PROJET META TABLE FIELD
ALTER TABLE public.c_attribute
  ADD COLUMN meta_table_ref character varying(256);

insert into c_attributetype (id, type) values (1, 'ENTIER');
insert into c_attributetype (id, type) values (2, 'TEXT');
insert into c_attributetype (id, type) values (3, 'DATE');
insert into c_attributetype (id, type) values (4, 'DOUBLE');
insert into c_attributetype (id, type) values (5, 'HEURE');
insert into c_attributetype (id, type) values (6, 'FICHIER');
insert into c_attributetype (id, type) values (7, 'Utilisateur');
insert into c_attributetype (id, type) values (8, 'Monétaire');
insert into c_attributetype (id, type) values (9, 'LOCK');
insert into c_attributetype (id, type) values (10, 'META TABLE');
insert into c_attributetype (id, type) values (11, 'META REFERENCE');

--  DATA UPDATE
--  DATE 21 JUL 2014
--  MINI PROJET EVENTS
ALTER TABLE public.e_event
  ADD COLUMN auto_event character not null default 'N';


--  DATA UPDATE
--  DATE 22 JUL 2014
--  MINI PROJET RELANCES
ALTER TABLE e_event
  ADD COLUMN differe character(1) NOT NULL DEFAULT 'N';
ALTER TABLE e_event
  ADD COLUMN date_lancement timestamp with time zone;
ALTER TABLE e_event
  ADD COLUMN periode integer;
ALTER TABLE e_event
  ADD COLUMN nb_relance integer;
ALTER TABLE e_event
  DROP CONSTRAINT e_event_destinataire_fkey;
ALTER TABLE e_event
  ADD CONSTRAINT e_event_destinataire_fkey FOREIGN KEY (destinataire)
      REFERENCES core_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

      
ALTER TABLE e_event_instance
  ADD COLUMN next_exec timestamp with time zone;
ALTER TABLE e_event_instance
  ADD COLUMN nb_exec integer;
ALTER TABLE e_event_instance
  DROP CONSTRAINT e_event_instance_event_fkey;
ALTER TABLE e_event_instance
  ADD CONSTRAINT e_event_instance_event_fkey FOREIGN KEY (event)
      REFERENCES e_event (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE e_event_instance
  ADD COLUMN dest_entity integer;
ALTER TABLE e_event_instance
  ADD COLUMN dest_row integer;

CREATE SEQUENCE public.e_event_dest_seq;
ALTER SEQUENCE public.e_event_dest_seq
  OWNER TO jakj;

CREATE TABLE public.e_event_dest
(
   id integer NOT NULL DEFAULT nextval('e_event_dest_seq'::regclass), 
   event integer NOT NULL, 
   destinataire integer, 
   PRIMARY KEY (id), 
   FOREIGN KEY (event) REFERENCES e_event (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.e_event_dest
  OWNER TO jakj;


CREATE SEQUENCE public.c_identification_row_seq;
ALTER SEQUENCE public.c_identification_row_seq
  OWNER TO jakj;


CREATE TABLE c_identification_row
(
  id integer NOT NULL DEFAULT nextval('c_identification_row_seq'::regclass),
  reference_row integer NOT NULL,
  reference_source integer NOT NULL,
  CONSTRAINT c_identification_row_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_identification_row
  OWNER TO jakj;


--  DATA UPDATE
--  DATE 04 AUG 2014
--  MINI Profiles and roles

-- DROP TABLE core_profil;

CREATE TABLE core_profil
(
  id serial NOT NULL,
  code character varying(4) NOT NULL,
  libelle character varying NOT NULL,
  date_effet date,
  date_fin date,
  id_role integer,
  CONSTRAINT pk_core_profil PRIMARY KEY (id),
  CONSTRAINT fk_core_profil_core_role FOREIGN KEY (id_role)
      REFERENCES core_role (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE core_profil
  OWNER TO jakj;


CREATE TABLE core_data_constraint
(
  id serial NOT NULL,
  id_role integer,
  entity integer,
  bean_id integer,
  CONSTRAINT pk_core_data_constraint PRIMARY KEY (id),
  CONSTRAINT fk_core_data_constraint_core_profil FOREIGN KEY (id_role)
      REFERENCES core_profil (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_core_data_constraint_c_business_class FOREIGN KEY (entity)
      REFERENCES c_businessclass (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

CREATE TABLE core_user_profiles
(
  user_id integer not null,
  profil_id integer not null,
  CONSTRAINT pk_core_user_profiles PRIMARY KEY (user_id,profil_id),
  CONSTRAINT fk_core_user_profiles_core_user FOREIGN KEY (user_id)
      REFERENCES core_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_core_user_profiles_core_profil FOREIGN KEY (profil_id)
      REFERENCES core_profil (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

--  DATA UPDATE
--  DATE 08 AUG 2014
--  MINI Profiles and roles
alter table core_user
  add column binding_entity integer default 0;

--  DATA UPDATE
--  DATE 08 AUG 2014
--  MINI PROJET Profiles and roles
CREATE TABLE core_acl_screen
(
  id serial not null,
  window_id integer not null,
  modification character(1) not null default 'Y',
  suppression character(1) not null default 'Y',
  CONSTRAINT pk_core_acl_screen PRIMARY KEY (id),
  CONSTRAINT fk_core_acl_screen_window FOREIGN KEY (window_id)
    REFERENCES c_window (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
CREATE TABLE core_acl_screen_attribute
(
  id serial not null,
  acl integer not null,
  attribute_id integer not null,
  CONSTRAINT pk_core_acl_screen_attribute PRIMARY KEY (id),
  CONSTRAINT fk_core_acl_attribute_acl FOREIGN KEY (acl)
    REFERENCES core_acl_screen (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_core_acl_attribute_attribute FOREIGN KEY (attribute_id)
    REFERENCES c_attribute (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
CREATE TABLE core_type_role
(
  id serial not null,
  libelle character varying (256) not null,
  CONSTRAINT pk_core_type_role PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE core_role
  ADD COLUMN profil integer not null default 0;
ALTER TABLE core_type_role
  ADD COLUMN appkey text;

--  DATA UPDATE
--  DATE 11 AUG 2014
--  MINI PROJET Advanced organizations framework

CREATE TABLE public.g_org_role
(
   id serial NOT NULL, 
   libelle character varying(128), 
   PRIMARY KEY (id)
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.g_org_role
  OWNER TO jakj;
ALTER TABLE g_organization
  ADD COLUMN role_id integer;
ALTER TABLE g_organization
  ADD FOREIGN KEY (role_id) REFERENCES g_org_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE TABLE public.g_structure_element
(
   id serial NOT NULL, 
   nom character varying(128), 
   role_id integer NOT NULL, 
   parent_id integer, 
   PRIMARY KEY (id), 
   FOREIGN KEY (role_id) REFERENCES g_org_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.g_structure_element
  OWNER TO jakj;
CREATE TABLE public.g_structure_template
(
   id serial NOT NULL, 
   nom character varying(128), 
   root integer, 
   PRIMARY KEY (id), 
   FOREIGN KEY (root) REFERENCES g_structure_element (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.g_structure_template
  OWNER TO jakj;
CREATE TABLE public.g_org_structure
(
   id serial NOT NULL, 
   nom character varying(128), 
   root integer, 
   template integer, 
   PRIMARY KEY (id), 
   FOREIGN KEY (root) REFERENCES g_organization (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (template) REFERENCES g_structure_template (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.g_org_structure
  OWNER TO jakj;
ALTER TABLE g_org_structure
  ADD COLUMN created_by integer NOT NULL;
ALTER TABLE g_org_structure
  ADD COLUMN appkey text;
ALTER TABLE g_org_structure
  ADD FOREIGN KEY (created_by) REFERENCES core_user (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE g_org_role
  ADD COLUMN appkey text;

--  DATA UPDATE
--  DATE 12 AUG 2014
--  MINI PROJET Advanced organizations framework
CREATE TABLE c_localizable_entity
(
  id serial NOT NULL,
  id_organzation integer,
  id_entity integer,
  CONSTRAINT c_localizable_entity_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_localizable_entity
  OWNER TO jakj;

CREATE TABLE c_localized_bean
(
  id serial NOT NULL,
  localization_id integer,
  bean_id integer,
  PRIMARY KEY (id), 
  FOREIGN KEY (localization_id) REFERENCES c_localizable_entity (id) 
    ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_localizable_entity
  OWNER TO jakj;
ALTER TABLE c_localized_bean
  ADD COLUMN org_id integer;
ALTER TABLE core_acl_screen
  ADD COLUMN role_id integer NOT NULL;
ALTER TABLE core_acl_screen
  ADD FOREIGN KEY (role_id) REFERENCES core_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE core_user
  ADD COLUMN profil integer;
ALTER TABLE s_rubrique
  ADD COLUMN technical character (1) NOT NULL DEFAULT 'N';

CREATE TABLE public.core_acl_action
(
   id serial NOT NULL, 
   role_id integer NOT NULL, 
   action_id integer NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (role_id) REFERENCES core_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (action_id) REFERENCES c_actionbutton (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.core_acl_action
  OWNER TO jakj;

CREATE TABLE public.core_acl_document
(
   id serial NOT NULL, 
   role_id integer NOT NULL, 
   document_id integer NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (role_id) REFERENCES core_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (document_id) REFERENCES c_documentbutton (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.core_acl_document
  OWNER TO jakj;

CREATE TABLE public.core_acl_procedure
(
   id serial NOT NULL, 
   role_id integer NOT NULL, 
   procedure_id integer NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (role_id) REFERENCES core_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (procedure_id) REFERENCES s_procedure (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.core_acl_procedure
  OWNER TO jakj;

CREATE TABLE public.core_acl_workflow
(
   id serial NOT NULL, 
   role_id integer NOT NULL, 
   workflow_id integer NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (role_id) REFERENCES core_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (workflow_id) REFERENCES s_wf_definition (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.core_acl_workflow
  OWNER TO jakj;

CREATE TABLE public.core_acl_system
(
   id serial NOT NULL, 
   role_id integer NOT NULL, 
   system_function character varying (256) NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (role_id) REFERENCES core_role (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.core_acl_system
  OWNER TO jakj;



CREATE SEQUENCE public.s_widget_user_seq;
ALTER SEQUENCE public.s_widget_user_seq
  OWNER TO jakj;


-- DROP TABLE s_widget_user;

CREATE TABLE s_widget_user
(
  id integer NOT NULL DEFAULT nextval('s_widget_user_seq'::regclass),
  id_widget integer,
  id_user integer,
  update_date timestamp without time zone,
  CONSTRAINT s_widget_user_pkey PRIMARY KEY (id),
  CONSTRAINT s_widget_user_id_user_fkey FOREIGN KEY (id_user)
      REFERENCES core_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT s_widget_user_id_widget_fkey FOREIGN KEY (id_widget)
      REFERENCES s_widget (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE s_widget_user
  OWNER TO jakj;

  CREATE SEQUENCE public.help_article_seq;
ALTER SEQUENCE public.help_article_seq
  OWNER TO jakj;
CREATE SEQUENCE public.help_tag_seq;
ALTER SEQUENCE public.help_tag_seq
  OWNER TO jakj;
CREATE SEQUENCE public.help_menu_seq;
ALTER SEQUENCE public.help_menu_seq
  OWNER TO jakj;
CREATE SEQUENCE public.help_article_link_seq;
ALTER SEQUENCE public.help_article_link_seq
  OWNER TO jakj;
CREATE SEQUENCE public.help_article_tag_seq;
ALTER SEQUENCE public.help_article_tag_seq
  OWNER TO jakj;
-- Table: help_article

-- DROP TABLE help_article;

CREATE TABLE help_article
(
  article_id integer NOT NULL DEFAULT nextval('help_article_seq'::regclass),
  title character varying(128),
  article_content text,
  video character varying(256),
  CONSTRAINT help_article_id_pk PRIMARY KEY (article_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE help_article
  OWNER TO jakj;


-- Table: help_tag

-- DROP TABLE help_tag;

CREATE TABLE help_tag
(
  tag_id integer NOT NULL DEFAULT nextval('help_tag_seq'::regclass),
  tag_label character varying(128),
  CONSTRAINT help_tag_id_pk PRIMARY KEY (tag_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE help_tag
  OWNER TO jakj;

-- Table: help_menu

-- DROP TABLE help_menu;

CREATE TABLE help_menu
(
  menu_id integer NOT NULL DEFAULT nextval('help_menu_seq'::regclass),
  title character varying(256),
  leaf boolean,
  parent_id integer,
  article_id integer,
  CONSTRAINT help_menu_id_pk PRIMARY KEY (menu_id),
  CONSTRAINT help_menu_article_id_fk FOREIGN KEY (article_id)
      REFERENCES help_article (article_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT help_menu_menu_parent_id_fk FOREIGN KEY (parent_id)
      REFERENCES help_menu (menu_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE help_menu
  OWNER TO jakj;

  INSERT into help_menu values(-1,'NO_PARENT',false,NULL,NULL);
-- Index: fki_help_menu_article_id_fk

-- DROP INDEX fki_help_menu_article_id_fk;

CREATE INDEX fki_help_menu_article_id_fk
  ON help_menu
  USING btree
  (article_id);

-- Index: fki_help_menu_menu_parent_id_fk

-- DROP INDEX fki_help_menu_menu_parent_id_fk;

CREATE INDEX fki_help_menu_menu_parent_id_fk
  ON help_menu
  USING btree
  (parent_id);

-- Table: help_article_link

-- DROP TABLE help_article_link;

CREATE TABLE help_article_link
(
  id integer NOT NULL DEFAULT nextval('help_article_link_seq'::regclass),
  src_article_id integer,
  dest_article_id integer,
  CONSTRAINT help_article_link_id_pk PRIMARY KEY (id),
  CONSTRAINT help_article_link_dest_article_id_fk FOREIGN KEY (dest_article_id)
      REFERENCES help_article (article_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT help_article_link_src_article_id_fk FOREIGN KEY (src_article_id)
      REFERENCES help_article (article_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE help_article_link
  OWNER TO jakj;

-- Index: fki_help_article_link_article_id_fk

-- DROP INDEX fki_help_article_link_article_id_fk;

CREATE INDEX fki_help_article_link_article_id_fk
  ON help_article_link
  USING btree
  (src_article_id);

-- Table: help_article_tag

-- DROP TABLE help_article_tag;

CREATE TABLE help_article_tag
(
  id integer NOT NULL DEFAULT nextval('help_article_tag_seq'::regclass),
  article_id integer,
  tag_id integer,
  CONSTRAINT help_article_tag_id_pk PRIMARY KEY (id),
  CONSTRAINT help_article_tag_article_id_fk FOREIGN KEY (article_id)
      REFERENCES help_article (article_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT help_article_tag_tag_id_fk FOREIGN KEY (tag_id)
      REFERENCES help_tag (tag_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE help_article_tag
  OWNER TO jakj;

-- Index: fki_help_article_tag_article_id_fk

-- DROP INDEX fki_help_article_tag_article_id_fk;

CREATE INDEX fki_help_article_tag_article_id_fk
  ON help_article_tag
  USING btree
  (article_id);

-- Index: fki_help_article_tag_tag_id_fk

-- DROP INDEX fki_help_article_tag_tag_id_fk;

CREATE INDEX fki_help_article_tag_tag_id_fk
  ON help_article_tag
  USING btree
  (tag_id);

  -- Table: help_question

-- DROP TABLE help_question;

CREATE TABLE help_question
(
  question_id integer NOT NULL DEFAULT nextval('help_tag_seq'::regclass),
  question_label character varying(500),
  question_queries bigint DEFAULT 0,
  CONSTRAINT help_question_id_pk PRIMARY KEY (question_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE help_question
  OWNER TO jakj;

 -- Table: help_article_question

-- DROP TABLE help_article_question;

CREATE TABLE help_article_question
(
  id integer NOT NULL DEFAULT nextval('help_article_tag_seq'::regclass),
  article_id integer,
  question_id integer,
  CONSTRAINT help_article_question_id_pk PRIMARY KEY (id),
  CONSTRAINT help_article_question_article_id_fk FOREIGN KEY (article_id)
      REFERENCES help_article (article_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT help_article_question_question_id_fk FOREIGN KEY (question_id)
      REFERENCES help_question (question_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE help_article_question
  OWNER TO jakj;

-- Index: fki_help_article_question_article_id_fk

-- DROP INDEX fki_help_article_question_article_id_fk;

CREATE INDEX fki_help_article_question_article_id_fk
  ON help_article_question
  USING btree
  (article_id);

-- Index: fki_help_article_question_question_id_fk

-- DROP INDEX fki_help_article_question_question_id_fk;

CREATE INDEX fki_help_article_question_question_id_fk
  ON help_article_question
  USING btree
  (question_id);


  
CREATE TABLE core_code_postal
(
  id serial not null,
  code_postal character varying (128),
  pays integer not null,
  CONSTRAINT pk_core_code_postal PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE core_code_postal
  OWNER TO jakj;

CREATE TABLE core_ville
(
  id serial not null,
  ville character varying (128),
  code_postal integer not null,
  CONSTRAINT pk_core_ville PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE core_ville
  OWNER TO jakj;



-- Table: "countriesInfo"

-- DROP TABLE "countriesInfo";

CREATE TABLE "countriesInfo"
(
  id integer,
  iso character varying(2),
  iso3 character varying(3),
  "cName" character varying(80),
  nicename character varying(80),
  numcode integer,
  phonecode integer
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "countriesInfo"
  OWNER TO jakj;


INSERT INTO "countriesInfo" (id, iso, "cName", nicename, iso3, numcode, phonecode) VALUES
(1, 'AF', 'AFGHANISTAN', 'Afghanistan', 'AFG', 4, 93),
(2, 'AL', 'ALBANIA', 'Albania', 'ALB', 8, 355),
(3, 'DZ', 'ALGERIA', 'Algeria', 'DZA', 12, 213),
(4, 'AS', 'AMERICAN SAMOA', 'American Samoa', 'ASM', 16, 1684),
(5, 'AD', 'ANDORRA', 'Andorra', 'AND', 20, 376),
(6, 'AO', 'ANGOLA', 'Angola', 'AGO', 24, 244),
(7, 'AI', 'ANGUILLA', 'Anguilla', 'AIA', 660, 1264),
(8, 'AQ', 'ANTARCTICA', 'Antarctica', NULL, NULL, 0),
(9, 'AG', 'ANTIGUA AND BARBUDA', 'Antigua and Barbuda', 'ATG', 28, 1268),
(10, 'AR', 'ARGENTINA', 'Argentina', 'ARG', 32, 54),
(11, 'AM', 'ARMENIA', 'Armenia', 'ARM', 51, 374),
(12, 'AW', 'ARUBA', 'Aruba', 'ABW', 533, 297),
(13, 'AU', 'AUSTRALIA', 'Australia', 'AUS', 36, 61),
(14, 'AT', 'AUSTRIA', 'Austria', 'AUT', 40, 43),
(15, 'AZ', 'AZERBAIJAN', 'Azerbaijan', 'AZE', 31, 994),
(16, 'BS', 'BAHAMAS', 'Bahamas', 'BHS', 44, 1242),
(17, 'BH', 'BAHRAIN', 'Bahrain', 'BHR', 48, 973),
(18, 'BD', 'BANGLADESH', 'Bangladesh', 'BGD', 50, 880),
(19, 'BB', 'BARBADOS', 'Barbados', 'BRB', 52, 1246),
(20, 'BY', 'BELARUS', 'Belarus', 'BLR', 112, 375),
(21, 'BE', 'BELGIUM', 'Belgium', 'BEL', 56, 32),
(22, 'BZ', 'BELIZE', 'Belize', 'BLZ', 84, 501),
(23, 'BJ', 'BENIN', 'Benin', 'BEN', 204, 229),
(24, 'BM', 'BERMUDA', 'Bermuda', 'BMU', 60, 1441),
(25, 'BT', 'BHUTAN', 'Bhutan', 'BTN', 64, 975),
(26, 'BO', 'BOLIVIA', 'Bolivia', 'BOL', 68, 591),
(27, 'BA', 'BOSNIA AND HERZEGOVINA', 'Bosnia and Herzegovina', 'BIH', 70, 387),
(28, 'BW', 'BOTSWANA', 'Botswana', 'BWA', 72, 267),
(29, 'BV', 'BOUVET ISLAND', 'Bouvet Island', NULL, NULL, 0),
(30, 'BR', 'BRAZIL', 'Brazil', 'BRA', 76, 55),
(31, 'IO', 'BRITISH INDIAN OCEAN TERRITORY', 'British Indian Ocean Territory', NULL, NULL, 246),
(32, 'BN', 'BRUNEI DARUSSALAM', 'Brunei Darussalam', 'BRN', 96, 673),
(33, 'BG', 'BULGARIA', 'Bulgaria', 'BGR', 100, 359),
(34, 'BF', 'BURKINA FASO', 'Burkina Faso', 'BFA', 854, 226),
(35, 'BI', 'BURUNDI', 'Burundi', 'BDI', 108, 257),
(36, 'KH', 'CAMBODIA', 'Cambodia', 'KHM', 116, 855),
(37, 'CM', 'CAMEROON', 'Cameroon', 'CMR', 120, 237),
(38, 'CA', 'CANADA', 'Canada', 'CAN', 124, 1),
(39, 'CV', 'CAPE VERDE', 'Cape Verde', 'CPV', 132, 238),
(40, 'KY', 'CAYMAN ISLANDS', 'Cayman Islands', 'CYM', 136, 1345),
(41, 'CF', 'CENTRAL AFRICAN REPUBLIC', 'Central African Republic', 'CAF', 140, 236),
(42, 'TD', 'CHAD', 'Chad', 'TCD', 148, 235),
(43, 'CL', 'CHILE', 'Chile', 'CHL', 152, 56),
(44, 'CN', 'CHINA', 'China', 'CHN', 156, 86),
(45, 'CX', 'CHRISTMAS ISLAND', 'Christmas Island', NULL, NULL, 61),
(46, 'CC', 'COCOS (KEELING) ISLANDS', 'Cocos (Keeling) Islands', NULL, NULL, 672),
(47, 'CO', 'COLOMBIA', 'Colombia', 'COL', 170, 57),
(48, 'KM', 'COMOROS', 'Comoros', 'COM', 174, 269),
(49, 'CG', 'CONGO', 'Congo', 'COG', 178, 242),
(50, 'CD', 'CONGO, THE DEMOCRATIC REPUBLIC OF THE', 'Congo, the Democratic Republic of the', 'COD', 180, 242),
(51, 'CK', 'COOK ISLANDS', 'Cook Islands', 'COK', 184, 682),
(52, 'CR', 'COSTA RICA', 'Costa Rica', 'CRI', 188, 506),
(53, 'CI', 'COTE D''IVOIRE', 'Cote D''Ivoire', 'CIV', 384, 225),
(54, 'HR', 'CROATIA', 'Croatia', 'HRV', 191, 385),
(55, 'CU', 'CUBA', 'Cuba', 'CUB', 192, 53),
(56, 'CY', 'CYPRUS', 'Cyprus', 'CYP', 196, 357),
(57, 'CZ', 'CZECH REPUBLIC', 'Czech Republic', 'CZE', 203, 420),
(58, 'DK', 'DENMARK', 'Denmark', 'DNK', 208, 45),
(59, 'DJ', 'DJIBOUTI', 'Djibouti', 'DJI', 262, 253),
(60, 'DM', 'DOMINICA', 'Dominica', 'DMA', 212, 1767),
(61, 'DO', 'DOMINICAN REPUBLIC', 'Dominican Republic', 'DOM', 214, 1809),
(62, 'EC', 'ECUADOR', 'Ecuador', 'ECU', 218, 593),
(63, 'EG', 'EGYPT', 'Egypt', 'EGY', 818, 20),
(64, 'SV', 'EL SALVADOR', 'El Salvador', 'SLV', 222, 503),
(65, 'GQ', 'EQUATORIAL GUINEA', 'Equatorial Guinea', 'GNQ', 226, 240),
(66, 'ER', 'ERITREA', 'Eritrea', 'ERI', 232, 291),
(67, 'EE', 'ESTONIA', 'Estonia', 'EST', 233, 372),
(68, 'ET', 'ETHIOPIA', 'Ethiopia', 'ETH', 231, 251),
(69, 'FK', 'FALKLAND ISLANDS (MALVINAS)', 'Falkland Islands (Malvinas)', 'FLK', 238, 500),
(70, 'FO', 'FAROE ISLANDS', 'Faroe Islands', 'FRO', 234, 298),
(71, 'FJ', 'FIJI', 'Fiji', 'FJI', 242, 679),
(72, 'FI', 'FINLAND', 'Finland', 'FIN', 246, 358),
(73, 'FR', 'FRANCE', 'France', 'FRA', 250, 33),
(74, 'GF', 'FRENCH GUIANA', 'French Guiana', 'GUF', 254, 594),
(75, 'PF', 'FRENCH POLYNESIA', 'French Polynesia', 'PYF', 258, 689),
(76, 'TF', 'FRENCH SOUTHERN TERRITORIES', 'French Southern Territories', NULL, NULL, 0),
(77, 'GA', 'GABON', 'Gabon', 'GAB', 266, 241),
(78, 'GM', 'GAMBIA', 'Gambia', 'GMB', 270, 220),
(79, 'GE', 'GEORGIA', 'Georgia', 'GEO', 268, 995),
(80, 'DE', 'GERMANY', 'Germany', 'DEU', 276, 49),
(81, 'GH', 'GHANA', 'Ghana', 'GHA', 288, 233),
(82, 'GI', 'GIBRALTAR', 'Gibraltar', 'GIB', 292, 350),
(83, 'GR', 'GREECE', 'Greece', 'GRC', 300, 30),
(84, 'GL', 'GREENLAND', 'Greenland', 'GRL', 304, 299),
(85, 'GD', 'GRENADA', 'Grenada', 'GRD', 308, 1473),
(86, 'GP', 'GUADELOUPE', 'Guadeloupe', 'GLP', 312, 590),
(87, 'GU', 'GUAM', 'Guam', 'GUM', 316, 1671),
(88, 'GT', 'GUATEMALA', 'Guatemala', 'GTM', 320, 502),
(89, 'GN', 'GUINEA', 'Guinea', 'GIN', 324, 224),
(90, 'GW', 'GUINEA-BISSAU', 'Guinea-Bissau', 'GNB', 624, 245),
(91, 'GY', 'GUYANA', 'Guyana', 'GUY', 328, 592),
(92, 'HT', 'HAITI', 'Haiti', 'HTI', 332, 509),
(93, 'HM', 'HEARD ISLAND AND MCDONALD ISLANDS', 'Heard Island and Mcdonald Islands', NULL, NULL, 0),
(94, 'VA', 'HOLY SEE (VATICAN CITY STATE)', 'Holy See (Vatican City State)', 'VAT', 336, 39),
(95, 'HN', 'HONDURAS', 'Honduras', 'HND', 340, 504),
(96, 'HK', 'HONG KONG', 'Hong Kong', 'HKG', 344, 852),
(97, 'HU', 'HUNGARY', 'Hungary', 'HUN', 348, 36),
(98, 'IS', 'ICELAND', 'Iceland', 'ISL', 352, 354),
(99, 'IN', 'INDIA', 'India', 'IND', 356, 91),
(100, 'ID', 'INDONESIA', 'Indonesia', 'IDN', 360, 62),
(101, 'IR', 'IRAN, ISLAMIC REPUBLIC OF', 'Iran, Islamic Republic of', 'IRN', 364, 98),
(102, 'IQ', 'IRAQ', 'Iraq', 'IRQ', 368, 964),
(103, 'IE', 'IRELAND', 'Ireland', 'IRL', 372, 353),
(104, 'IL', 'ISRAEL', 'Israel', 'ISR', 376, 972),
(105, 'IT', 'ITALY', 'Italy', 'ITA', 380, 39),
(106, 'JM', 'JAMAICA', 'Jamaica', 'JAM', 388, 1876),
(107, 'JP', 'JAPAN', 'Japan', 'JPN', 392, 81),
(108, 'JO', 'JORDAN', 'Jordan', 'JOR', 400, 962),
(109, 'KZ', 'KAZAKHSTAN', 'Kazakhstan', 'KAZ', 398, 7),
(110, 'KE', 'KENYA', 'Kenya', 'KEN', 404, 254),
(111, 'KI', 'KIRIBATI', 'Kiribati', 'KIR', 296, 686),
(112, 'KP', 'KOREA, DEMOCRATIC PEOPLE''S REPUBLIC OF', 'Korea, Democratic People''s Republic of', 'PRK', 408, 850),
(113, 'KR', 'KOREA, REPUBLIC OF', 'Korea, Republic of', 'KOR', 410, 82),
(114, 'KW', 'KUWAIT', 'Kuwait', 'KWT', 414, 965),
(115, 'KG', 'KYRGYZSTAN', 'Kyrgyzstan', 'KGZ', 417, 996),
(116, 'LA', 'LAO PEOPLE''S DEMOCRATIC REPUBLIC', 'Lao People''s Democratic Republic', 'LAO', 418, 856),
(117, 'LV', 'LATVIA', 'Latvia', 'LVA', 428, 371),
(118, 'LB', 'LEBANON', 'Lebanon', 'LBN', 422, 961),
(119, 'LS', 'LESOTHO', 'Lesotho', 'LSO', 426, 266),
(120, 'LR', 'LIBERIA', 'Liberia', 'LBR', 430, 231),
(121, 'LY', 'LIBYAN ARAB JAMAHIRIYA', 'Libyan Arab Jamahiriya', 'LBY', 434, 218),
(122, 'LI', 'LIECHTENSTEIN', 'Liechtenstein', 'LIE', 438, 423),
(123, 'LT', 'LITHUANIA', 'Lithuania', 'LTU', 440, 370),
(124, 'LU', 'LUXEMBOURG', 'Luxembourg', 'LUX', 442, 352),
(125, 'MO', 'MACAO', 'Macao', 'MAC', 446, 853),
(126, 'MK', 'MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF', 'Macedonia, the Former Yugoslav Republic of', 'MKD', 807, 389),
(127, 'MG', 'MADAGASCAR', 'Madagascar', 'MDG', 450, 261),
(128, 'MW', 'MALAWI', 'Malawi', 'MWI', 454, 265),
(129, 'MY', 'MALAYSIA', 'Malaysia', 'MYS', 458, 60),
(130, 'MV', 'MALDIVES', 'Maldives', 'MDV', 462, 960),
(131, 'ML', 'MALI', 'Mali', 'MLI', 466, 223),
(132, 'MT', 'MALTA', 'Malta', 'MLT', 470, 356),
(133, 'MH', 'MARSHALL ISLANDS', 'Marshall Islands', 'MHL', 584, 692),
(134, 'MQ', 'MARTINIQUE', 'Martinique', 'MTQ', 474, 596),
(135, 'MR', 'MAURITANIA', 'Mauritania', 'MRT', 478, 222),
(136, 'MU', 'MAURITIUS', 'Mauritius', 'MUS', 480, 230),
(137, 'YT', 'MAYOTTE', 'Mayotte', NULL, NULL, 269),
(138, 'MX', 'MEXICO', 'Mexico', 'MEX', 484, 52),
(139, 'FM', 'MICRONESIA, FEDERATED STATES OF', 'Micronesia, Federated States of', 'FSM', 583, 691),
(140, 'MD', 'MOLDOVA, REPUBLIC OF', 'Moldova, Republic of', 'MDA', 498, 373),
(141, 'MC', 'MONACO', 'Monaco', 'MCO', 492, 377),
(142, 'MN', 'MONGOLIA', 'Mongolia', 'MNG', 496, 976),
(143, 'MS', 'MONTSERRAT', 'Montserrat', 'MSR', 500, 1664),
(144, 'MA', 'MOROCCO', 'Morocco', 'MAR', 504, 212),
(145, 'MZ', 'MOZAMBIQUE', 'Mozambique', 'MOZ', 508, 258),
(146, 'MM', 'MYANMAR', 'Myanmar', 'MMR', 104, 95),
(147, 'NA', 'NAMIBIA', 'Namibia', 'NAM', 516, 264),
(148, 'NR', 'NAURU', 'Nauru', 'NRU', 520, 674),
(149, 'NP', 'NEPAL', 'Nepal', 'NPL', 524, 977),
(150, 'NL', 'NETHERLANDS', 'Netherlands', 'NLD', 528, 31),
(151, 'AN', 'NETHERLANDS ANTILLES', 'Netherlands Antilles', 'ANT', 530, 599),
(152, 'NC', 'NEW CALEDONIA', 'New Caledonia', 'NCL', 540, 687),
(153, 'NZ', 'NEW ZEALAND', 'New Zealand', 'NZL', 554, 64),
(154, 'NI', 'NICARAGUA', 'Nicaragua', 'NIC', 558, 505),
(155, 'NE', 'NIGER', 'Niger', 'NER', 562, 227),
(156, 'NG', 'NIGERIA', 'Nigeria', 'NGA', 566, 234),
(157, 'NU', 'NIUE', 'Niue', 'NIU', 570, 683),
(158, 'NF', 'NORFOLK ISLAND', 'Norfolk Island', 'NFK', 574, 672),
(159, 'MP', 'NORTHERN MARIANA ISLANDS', 'Northern Mariana Islands', 'MNP', 580, 1670),
(160, 'NO', 'NORWAY', 'Norway', 'NOR', 578, 47),
(161, 'OM', 'OMAN', 'Oman', 'OMN', 512, 968),
(162, 'PK', 'PAKISTAN', 'Pakistan', 'PAK', 586, 92),
(163, 'PW', 'PALAU', 'Palau', 'PLW', 585, 680),
(164, 'PS', 'PALESTINIAN TERRITORY, OCCUPIED', 'Palestinian Territory, Occupied', NULL, NULL, 970),
(165, 'PA', 'PANAMA', 'Panama', 'PAN', 591, 507),
(166, 'PG', 'PAPUA NEW GUINEA', 'Papua New Guinea', 'PNG', 598, 675),
(167, 'PY', 'PARAGUAY', 'Paraguay', 'PRY', 600, 595),
(168, 'PE', 'PERU', 'Peru', 'PER', 604, 51),
(169, 'PH', 'PHILIPPINES', 'Philippines', 'PHL', 608, 63),
(170, 'PN', 'PITCAIRN', 'Pitcairn', 'PCN', 612, 0),
(171, 'PL', 'POLAND', 'Poland', 'POL', 616, 48),
(172, 'PT', 'PORTUGAL', 'Portugal', 'PRT', 620, 351),
(173, 'PR', 'PUERTO RICO', 'Puerto Rico', 'PRI', 630, 1787),
(174, 'QA', 'QATAR', 'Qatar', 'QAT', 634, 974),
(175, 'RE', 'REUNION', 'Reunion', 'REU', 638, 262),
(176, 'RO', 'ROMANIA', 'Romania', 'ROM', 642, 40),
(177, 'RU', 'RUSSIAN FEDERATION', 'Russian Federation', 'RUS', 643, 70),
(178, 'RW', 'RWANDA', 'Rwanda', 'RWA', 646, 250),
(179, 'SH', 'SAINT HELENA', 'Saint Helena', 'SHN', 654, 290),
(180, 'KN', 'SAINT KITTS AND NEVIS', 'Saint Kitts and Nevis', 'KNA', 659, 1869),
(181, 'LC', 'SAINT LUCIA', 'Saint Lucia', 'LCA', 662, 1758),
(182, 'PM', 'SAINT PIERRE AND MIQUELON', 'Saint Pierre and Miquelon', 'SPM', 666, 508),
(183, 'VC', 'SAINT VINCENT AND THE GRENADINES', 'Saint Vincent and the Grenadines', 'VCT', 670, 1784),
(184, 'WS', 'SAMOA', 'Samoa', 'WSM', 882, 684),
(185, 'SM', 'SAN MARINO', 'San Marino', 'SMR', 674, 378),
(186, 'ST', 'SAO TOME AND PRINCIPE', 'Sao Tome and Principe', 'STP', 678, 239),
(187, 'SA', 'SAUDI ARABIA', 'Saudi Arabia', 'SAU', 682, 966),
(188, 'SN', 'SENEGAL', 'Senegal', 'SEN', 686, 221),
(189, 'CS', 'SERBIA AND MONTENEGRO', 'Serbia and Montenegro', NULL, NULL, 381),
(190, 'SC', 'SEYCHELLES', 'Seychelles', 'SYC', 690, 248),
(191, 'SL', 'SIERRA LEONE', 'Sierra Leone', 'SLE', 694, 232),
(192, 'SG', 'SINGAPORE', 'Singapore', 'SGP', 702, 65),
(193, 'SK', 'SLOVAKIA', 'Slovakia', 'SVK', 703, 421),
(194, 'SI', 'SLOVENIA', 'Slovenia', 'SVN', 705, 386),
(195, 'SB', 'SOLOMON ISLANDS', 'Solomon Islands', 'SLB', 90, 677),
(196, 'SO', 'SOMALIA', 'Somalia', 'SOM', 706, 252),
(197, 'ZA', 'SOUTH AFRICA', 'South Africa', 'ZAF', 710, 27),
(198, 'GS', 'SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS', 'South Georgia and the South Sandwich Islands', NULL, NULL, 0),
(199, 'ES', 'SPAIN', 'Spain', 'ESP', 724, 34),
(200, 'LK', 'SRI LANKA', 'Sri Lanka', 'LKA', 144, 94),
(201, 'SD', 'SUDAN', 'Sudan', 'SDN', 736, 249),
(202, 'SR', 'SURINAME', 'Suriname', 'SUR', 740, 597),
(203, 'SJ', 'SVALBARD AND JAN MAYEN', 'Svalbard and Jan Mayen', 'SJM', 744, 47),
(204, 'SZ', 'SWAZILAND', 'Swaziland', 'SWZ', 748, 268),
(205, 'SE', 'SWEDEN', 'Sweden', 'SWE', 752, 46),
(206, 'CH', 'SWITZERLAND', 'Switzerland', 'CHE', 756, 41),
(207, 'SY', 'SYRIAN ARAB REPUBLIC', 'Syrian Arab Republic', 'SYR', 760, 963),
(208, 'TW', 'TAIWAN, PROVINCE OF CHINA', 'Taiwan, Province of China', 'TWN', 158, 886),
(209, 'TJ', 'TAJIKISTAN', 'Tajikistan', 'TJK', 762, 992),
(210, 'TZ', 'TANZANIA, UNITED REPUBLIC OF', 'Tanzania, United Republic of', 'TZA', 834, 255),
(211, 'TH', 'THAILAND', 'Thailand', 'THA', 764, 66),
(212, 'TL', 'TIMOR-LESTE', 'Timor-Leste', NULL, NULL, 670),
(213, 'TG', 'TOGO', 'Togo', 'TGO', 768, 228),
(214, 'TK', 'TOKELAU', 'Tokelau', 'TKL', 772, 690),
(215, 'TO', 'TONGA', 'Tonga', 'TON', 776, 676),
(216, 'TT', 'TRINIDAD AND TOBAGO', 'Trinidad and Tobago', 'TTO', 780, 1868),
(217, 'TN', 'TUNISIA', 'Tunisia', 'TUN', 788, 216),
(218, 'TR', 'TURKEY', 'Turkey', 'TUR', 792, 90),
(219, 'TM', 'TURKMENISTAN', 'Turkmenistan', 'TKM', 795, 7370),
(220, 'TC', 'TURKS AND CAICOS ISLANDS', 'Turks and Caicos Islands', 'TCA', 796, 1649),
(221, 'TV', 'TUVALU', 'Tuvalu', 'TUV', 798, 688),
(222, 'UG', 'UGANDA', 'Uganda', 'UGA', 800, 256),
(223, 'UA', 'UKRAINE', 'Ukraine', 'UKR', 804, 380),
(224, 'AE', 'UNITED ARAB EMIRATES', 'United Arab Emirates', 'ARE', 784, 971),
(225, 'GB', 'UNITED KINGDOM', 'United Kingdom', 'GBR', 826, 44),
(226, 'US', 'UNITED STATES', 'United States', 'USA', 840, 1),
(227, 'UM', 'UNITED STATES MINOR OUTLYING ISLANDS', 'United States Minor Outlying Islands', NULL, NULL, 1),
(228, 'UY', 'URUGUAY', 'Uruguay', 'URY', 858, 598),
(229, 'UZ', 'UZBEKISTAN', 'Uzbekistan', 'UZB', 860, 998),
(230, 'VU', 'VANUATU', 'Vanuatu', 'VUT', 548, 678),
(231, 'VE', 'VENEZUELA', 'Venezuela', 'VEN', 862, 58),
(232, 'VN', 'VIET NAM', 'Viet Nam', 'VNM', 704, 84),
(233, 'VG', 'VIRGIN ISLANDS, BRITISH', 'Virgin Islands, British', 'VGB', 92, 1284),
(234, 'VI', 'VIRGIN ISLANDS, U.S.', 'Virgin Islands, U.s.', 'VIR', 850, 1340),
(235, 'WF', 'WALLIS AND FUTUNA', 'Wallis and Futuna', 'WLF', 876, 681),
(237, 'YE', 'YEMEN', 'Yemen', 'YEM', 887, 967),
(238, 'ZM', 'ZAMBIA', 'Zambia', 'ZMB', 894, 260),
(239, 'ZW', 'ZIMBABWE', 'Zimbabwe', 'ZWE', 716, 263);


alter table core_type_role
  add column description text;

-- Table: s_dashboard_user

-- DROP TABLE s_dashboard_user;

CREATE SEQUENCE s_dashboard_user_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE s_dashboard_user_seq
  OWNER TO jakj;


CREATE TABLE s_dashboard_user
(
  id integer NOT NULL DEFAULT nextval('s_dashboard_user_seq'::regclass),
  id_user integer,
  appkey character varying(64),
  type character varying(15),
  CONSTRAINT s_dashboard_user_pkey PRIMARY KEY (id),
  CONSTRAINT s_dashboard_user_id_user_fkey FOREIGN KEY (id_user)
      REFERENCES core_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE s_dashboard_user
  OWNER TO jakj;
alter table s_widget add column created_by integer;

-- Type de données boolean
insert into c_attributetype (id,type) values ('12','Booléen');

  -- 'textarea'(Y|N) permet de distinguer entre champ de text simple ou textarea.
ALTER TABLE c_attribute ADD COLUMN textarea character DEFAULT 'N';

-- ======================================================
--      tableau de board décisionnel
-- =====================================================
    CREATE SEQUENCE c_fact_table_attribute_seq
  INCREMENT 1
  MINVALUE 10
  MAXVALUE 9223372036854775807
  START 76
  CACHE 1;
ALTER TABLE c_fact_table_attribute_seq
  OWNER TO jakj;
 
 
  -- Sequence: c_fact_table_seq
 
-- DROP SEQUENCE c_fact_table_seq;
 
CREATE SEQUENCE c_fact_table_seq
  INCREMENT 1
  MINVALUE 10
  MAXVALUE 9223372036854775807
  START 76
  CACHE 1;
ALTER TABLE c_fact_table_seq
  OWNER TO jakj;
 
 
 
  -- Table: c_fact_table
 
-- DROP TABLE c_fact_table;
 
CREATE TABLE c_fact_table
(
  id integer NOT NULL DEFAULT nextval('c_fact_table_seq'::regclass),
  tablename character varying(128),
  query text,
  appkey text,
  CONSTRAINT pk_c_fact_table PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_fact_table
  OWNER TO jakj;
 
 
-- Table: c_fact_table_attribute
 
-- DROP TABLE c_fact_table_attribute;
 
CREATE TABLE c_fact_table_attribute
(
  id integer NOT NULL DEFAULT nextval('c_fact_table_attribute_seq'::regclass),
  attribute_id integer NOT NULL,
  labelattribute character varying(128),
  isindex character(1) DEFAULT 'N'::bpchar,
  isxdimension character(1) DEFAULT 'N'::bpchar,
  isydimension character(1) DEFAULT 'N'::bpchar,
  isgroupby character(1) DEFAULT 'N'::bpchar,
  aggregationfct character varying(64),
  constraints text,
  fact_table_id integer,
  CONSTRAINT pk_c_fact_table_attribute PRIMARY KEY (id),
  CONSTRAINT c_fact_table_attribute_attributeid_fkey FOREIGN KEY (attribute_id)
      REFERENCES c_attribute (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT c_fact_table_attribute_fact_table_id_fkey FOREIGN KEY (fact_table_id)
      REFERENCES c_fact_table (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_fact_table_attribute
  OWNER TO jakj;
--------------------------------
--------------------------------
----  Scheduled entities
--------------------------------
CREATE TABLE c_scheduled_entity
(
  id serial NOT NULL,
  entity_id int not null,
  attribute_id int not null,
  window_id int not null,
  constraint pk_c_scheduled_entity primary key (id),
  constraint fk_c_scheduled_entity_entity FOREIGN key (entity_id)
    REFERENCES c_businessclass (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  constraint fk_c_scheduled_entity_attribute FOREIGN key (attribute_id)
    REFERENCES c_attribute (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  constraint fk_c_scheduled_entity_window FOREIGN key (window_id)
    REFERENCES c_window (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_scheduled_entity
  OWNER TO jakj;

---------------------------------

-- Sequence: c_role__fact_table_seq

-- DROP SEQUENCE c_role__fact_table_seq;

CREATE SEQUENCE c_role__fact_table_seq
  INCREMENT 1
  MINVALUE 10
  MAXVALUE 9223372036854775807
  START 97
  CACHE 1;
ALTER TABLE c_role__fact_table_seq
  OWNER TO jakj;

-- Table: c_role__fact_table

-- DROP TABLE c_role__fact_table;

CREATE TABLE c_role__fact_table
(
  role_id integer,
  fact_table_id integer,
  id integer NOT NULL DEFAULT nextval('c_role__fact_table_seq'::regclass),
  CONSTRAINT c_role__fact_table_pkey PRIMARY KEY (id),
  CONSTRAINT c_role__fact_table_fact_table_id_fkey FOREIGN KEY (fact_table_id)
      REFERENCES c_fact_table (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT c_role__fact_table_role_id_fkey FOREIGN KEY (role_id)
      REFERENCES core_role (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT c_role__fact_table_role_id_fact_table_id_key UNIQUE (role_id, fact_table_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE c_role__fact_table
  OWNER TO jakj;


ALTER TABLE c_fact_table 
	ADD COLUMN view_name character varying (128);

------------------------------------------------
--	File Storage
------------------------------------------------
CREATE TABLE c_entity_store
(
	id serial not null,
	class_id integer not null,
	bean_id integer not null,
	file_name character varying (1024) not null,
	CONSTRAINT c_entity_store_pkey PRIMARY KEY (id),
	CONSTRAINT c_entity_store_business_class FOREIGN KEY (class_id)
		REFERENCES c_businessclass (id) MATCH SIMPLE
		ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
	OIDS=FALSE
);
ALTER TABLE c_entity_store
	OWNER TO jakj;

ALTER TABLE c_businessclass
	ADD COLUMN storable_entity character (1) DEFAULT 'N';
	
CREATE TABLE c_file_type
(
	id serial not null,
	libelle character varying (512),
	CONSTRAINT c_file_type_pkey PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
);
ALTER TABLE c_file_type
	OWNER TO jakj;

ALTER TABLE c_entity_store
  ADD COLUMN type_id integer;
ALTER TABLE c_entity_store
  ADD FOREIGN KEY (type_id) REFERENCES c_file_type (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE c_entity_store
  ADD COLUMN libelle character varying (256);
  
ALTER TABLE c_entity_store
  ADD COLUMN description text;
  
ALTER TABLE c_entity_store
  ADD COLUMN is_private character(1) not null default 'N';

ALTER TABLE c_file_type
	ADD COLUMN appkey text;
	
--
--	Data constraint on windows
--
alter table c_window
	add column selection_constraint character varying (512);

	
ALTER TABLE c_scheduled_entity
  ADD COLUMN end_attribute integer NOT NULL DEFAULT 0;
  
  
--
--	Views
--
CREATE TABLE c_view
(
	id serial not null,
	title character varying (256) not null,
	view_type character (1) not null default 'E',
	window_id integer not null,
	constraint pk_c_view primary key (id),
	CONSTRAINT c_view_window FOREIGN KEY (window_id)
		REFERENCES c_window (id) MATCH SIMPLE
		ON UPDATE NO ACTION ON DELETE NO ACTION
)WITH (
	OIDS=FALSE
);
ALTER TABLE c_view
	OWNER TO jakj;
	
CREATE TABLE c_view_part
(
	id serial not null,
	titles text not null,
	query text not null,
	view_id integer not null,
	constraint pk_c_view_part primary key (id),
	constraint c_part_view foreign key (view_id)
		references c_view (id) MATCH SIMPLE
		ON UPDATE NO ACTION ON DELETE NO ACTION
)WITH (
	OIDS=FALSE
);
ALTER TABLE c_view_part
	OWNER TO jakj;

ALTER TABLE c_attribute
	ADD COLUMN mail_attribute character (1) DEFAULT 'N';
	
ALTER TABLE c_window
	ADD COLUMN mail_store character (1) DEFAULT 'N';
	
	
create table c_user_mailconfig
(
 id serial not null,
 host character varying (512) not null,
 pop character varying (64) ,
 inbox character varying (128) ,
 smtp character varying (64) ,
 login character varying(256),
 pass character varying(256),
 user_id integer not null
)WITH (
  OIDS=FALSE
);
ALTER TABLE c_user_mailconfig
  OWNER TO jakj;
  
ALTER TABLE c_user_mailconfig
  ADD PRIMARY KEY (id);

create table m_mail_cache
(
	id serial not null,
	dateMaj timestamp with time zone not null,
	taille integer not null default 50,
	user_id integer not null,
	constraint m_mail_cache_pk primary key (id),
	constraint m_mail_cache_user foreign key (user_id)
		references core_user (id) match simple
		on update no action on delete no action
)with (
	OIDS=FALSE
);
alter table m_mail_cache
	owner to jakj;
	
create table m_user_mail
(
	id serial not null,
	sujet character varying (512),
	correspondant character varying (512),
	dateMail timestamp with time zone,
	content text,
	sense character (1) default 'I',
	cache_id integer not null,
	user_id integer not null,
	constraint m_user_mail_pk primary key (id),
	constraint m_user_mail_cache foreign key (cache_id)
		references m_mail_cache (id) match simple
		on update no action on delete no action,
	constraint m_user_mail_user foreign key (user_id)
		references core_user (id) match simple
		on update no action on delete no action
)
with (
	OIDS=FALSE
);
alter table m_user_mail
	owner to jakj;

--	Creation de compte
	
create table core_code_postal
(
	id serial not null,
	code character varying (128),
	pays_id integer not null,
	constraint core_cp_pk primary key (id),
	constraint core_cp_pays foreign key (pays_id)
		references countriesInfo (id) match simple
		on update no action on delete no action
)
with (
	OIDS=FALSE
);
alter table core_code_postal
	owner to jakj;

create table core_ville 
(
	id serial not null,
	nom character varying (256),
	cp_id integer not null,
	constraint core_ville_pk primary key (id),
	constraint core_ville_cp foreign key (pays_id)
		references core_code_postal (id) match simple
		on update no action on delete no action
)
with (
	OIDS=FALSE
);
alter table core_ville
	owner to jakj;
	
create table core_user_info
(
	id serial not null,
	info_key character varying (512),
	info_value text,
	user_id integer not null,
	constraint core_user_info_pk primary key (id),
	constraint core_info_user foreign key (user_id)
		references core_user (id) match simple
		on update no action on delete no action
)
with (
	OIDS=FALSE
);
alter table core_user_info
	owner to jakj;
	
alter table c_attribute
	add column field_width integer not null default (120);
	
	
CREATE TABLE public.c_table_trans
(
   id serial NOT NULL, 
   id_class integer, 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_class) REFERENCES c_businessclass (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_table_trans
  OWNER TO jakj;
	
CREATE TABLE public.c_attribute_trans
(
   id serial NOT NULL, 
   id_attribute integer, 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_attribute) REFERENCES c_attribute (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_attribute_trans
  OWNER TO jakj;
  	
CREATE TABLE public.c_window_trans
(
   id serial NOT NULL, 
   id_window integer, 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_window) REFERENCES c_window (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_window_trans
  OWNER TO jakj;
	
CREATE TABLE public.c_rubrique_trans
(
   id serial NOT NULL, 
   id_rubrique integer, 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_rubrique) REFERENCES s_rubrique (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_rubrique_trans
  OWNER TO jakj;
  	
CREATE TABLE public.c_menu_trans
(
   id serial NOT NULL, 
   id_menu integer, 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_menu) REFERENCES s_menuitem (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_menu_trans
  OWNER TO jakj;

CREATE TABLE public.c_action_trans
(
   id serial NOT NULL, 
   id_action integer, 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_action) REFERENCES c_actionbutton (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_action_trans
  OWNER TO jakj;
  
CREATE TABLE public.c_document_trans
(
   id serial NOT NULL, 
   id_document integer, 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_document) REFERENCES c_documentbutton (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_document_trans
  OWNER TO jakj;
  
CREATE TABLE public.c_view_trans
(
   id serial NOT NULL, 
   id_view integer, 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id), 
   FOREIGN KEY (id_view) REFERENCES c_view (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_view_trans
  OWNER TO jakj;

CREATE TABLE public.application_translation
(
   id serial NOT NULL, 
   app_key character varying(64), 
   code_lang character varying(32), 
   val text, 
   PRIMARY KEY (id)
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.application_translation
  OWNER TO jakj;

alter table core_user
	add column lang character varying (16) not null default 'fr';

alter table public.application_translation
	add column appkey text;
	

/*
 * Validation attribute
 */
CREATE TABLE public.c_validation_action
(
   id serial NOT NULL, 
   type_action character varying(64) NOT NULL DEFAULT 'GENKEY', 
   arguments text, 
   formula text, 
   attribute_id integer NOT NULL, 
   PRIMARY KEY (id), 
   FOREIGN KEY (attribute_id) REFERENCES c_attribute (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_validation_action
  OWNER TO jakj;

CREATE TABLE public.f_filestore
(
   id serial NOT NULL, 
   file_title character varying(512), 
   full_file_path text, 
   table_data_reference character varying(256), 
   PRIMARY KEY (id)
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.f_filestore
  OWNER TO jakj;

ALTER TABLE f_filestore
	ADD COLUMN identifiant integer;
	
	
CREATE TABLE public.c_batch
(
   id serial NOT NULL, 
   nom character varying(512), 
   code character varying(512), 
   PRIMARY KEY (id)
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_batch
  OWNER TO jakj;

CREATE TABLE public.c_batch_argument
(
   id serial NOT NULL, 
   libelle character varying(512), 
   code character varying(512), 
   value text, 
   batch_id integer,
   PRIMARY KEY (id),
   CONSTRAINT c_batch_fkey FOREIGN KEY (batch_id)
      REFERENCES c_batch (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_batch_argument
  OWNER TO jakj;

CREATE TABLE public.c_batch_unit_type
(
   id serial NOT NULL, 
   libelle character varying(512), 
   PRIMARY KEY (id)
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_batch_unit_type
  OWNER TO jakj;

CREATE TABLE public.c_batch_unit
(
   id serial NOT NULL, 
   nom character varying(512), 
   instructionsModel text, 
   unit_order integer,
   batch_id integer,
   type_id integer,
   PRIMARY KEY (id),
   CONSTRAINT c_batch_fkey FOREIGN KEY (batch_id)
      REFERENCES c_batch (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT c_batch_type_fkey FOREIGN KEY (type_id)
      REFERENCES c_batch_unit_type (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_batch_unit
  OWNER TO jakj;
  
ALTER TABLE c_batch
	ADD COLUMN appkey text;

insert into c_batch_unit_type (id, libelle) values (1,'Insertion');
insert into c_batch_unit_type (id, libelle) values (2,'Modification');
insert into c_batch_unit_type (id, libelle) values (3,'Suppression');
insert into c_batch_unit_type (id, libelle) values (4,'Chargement');
insert into c_batch_unit_type (id, libelle) values (5,'Calcul');
insert into c_batch_unit_type (id, libelle) values (6,'Génération');

create table public.c_callout
(
   id serial NOT NULL, 
   nom character varying(512),
   fichier bytea,
   arguments text,
   appkey text,
   PRIMARY KEY (id)
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE public.c_callout
  OWNER TO jakj;
  
create table public.c_window_callout
(
	id serial NOT NULL,
	window_id integer,
	callout_id integer,
	PRIMARY KEY (id),
	CONSTRAINT fkey_window_callout_window FOREIGN KEY (window_id)
      REFERENCES c_window (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fkey_window_callout_callout FOREIGN KEY (callout_id)
      REFERENCES c_callout (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
	OIDS = FALSE
)
;
ALTER TABLE public.c_window_callout
  OWNER TO jakj;

create table public.c_win_call_argument
(
	id serial NOT NULL,
	window_callout_id integer,
	attribute_id integer,
	callout_arg character varying (256),
	prompt_opt character(1) NOT NULL default 'N',
	selection_opt character(1) NOT NULL default 'N',
	created_opt character(1) NOT NULL default 'N',
	PRIMARY KEY (id),
	CONSTRAINT fkey_wcw_callout FOREIGN KEY (window_callout_id)
      REFERENCES c_window_callout (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fkey_wcw_attribute FOREIGN KEY (attribute_id)
      REFERENCES c_attribute (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
;
ALTER TABLE public.c_win_call_argument
  OWNER TO jakj;

  CREATE TABLE public.g_resource
(
  id serial not null,
  res_name character varying(512),
  res_key character varying(512),
  res_type character varying(512),
  fichier bytea,
  CONSTRAINT g_resource_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.g_resource
  OWNER TO jakj;

CREATE TABLE public.c_window_synthesis
(
  id serial NOT NULL,
  s_title character varying(512),
  s_expression character varying(512),
  id_window integer NOT NULL,
  CONSTRAINT c_window_synthesis_pkey PRIMARY KEY (id),
  CONSTRAINT c_window_synthesis_window_fkey FOREIGN KEY (id_window)
      REFERENCES public.c_window (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.c_window_synthesis
  OWNER TO jakj;

ALTER TABLE c_window_callout
  ADD COLUMN c_type integer NOT NULL DEFAULT 1;

ALTER TABLE c_window_callout
  ADD COLUMN arg_map text;

  
--	FOLDERS AND CMS
ALTER TABLE c_callout
	ADD COLUMN callout_key integer;
update c_callout set callout_key=id;
  
CREATE TABLE public.c_folder
(
  id serial NOT NULL,
  folder_name text,
  is_root character(1) DEFAULT 'Y'::bpchar,
  id_parent integer,
  folder_description text,
  CONSTRAINT c_folder_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.c_folder
  OWNER TO jakj;


CREATE TABLE public.c_window_folder
(
  id serial NOT NULL,
  id_folder integer,
  id_window integer,
  CONSTRAINT c_window_folder_pkey PRIMARY KEY (id),
  CONSTRAINT fkey_window_folder_folder FOREIGN KEY (id_folder)
      REFERENCES public.c_folder (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkey_window_folder_window FOREIGN KEY (id_window)
      REFERENCES public.c_window (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.c_window_folder
  OWNER TO jakj;

CREATE TABLE public.c_document_type
(
  id serial NOT NULL,
  type_label text,
  CONSTRAINT c_document_type_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.c_document_type
  OWNER TO jakj;

CREATE TABLE public.row_document
(
  id serial NOT NULL,
  file_name text,
  file_extension text,
  date_creation timestamp with time zone,
  file_version integer DEFAULT 1,
  file_signature text,
  text_content text,
  storage_identifier text,
  storage_callout_id integer,
  id_row integer,
  id_window integer,
  id_type integer,
  id_folder integer,
  CONSTRAINT c_document_pkey PRIMARY KEY (id),
  CONSTRAINT c_document_folder_fkey FOREIGN KEY (id_folder)
      REFERENCES public.c_folder (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT c_document_type_fkey FOREIGN KEY (id_type)
      REFERENCES public.c_document_type (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT c_document_window_fkey FOREIGN KEY (id_window)
      REFERENCES public.c_window (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.row_document
  OWNER TO jakj;

ALTER TABLE public.row_document
	ADD COLUMN id_user integer;
  
CREATE TABLE public.row_doc_comment
(
  id serial NOT NULL,
  comment_title text,
  comment_text text,
  date_comment timestamp with time zone,
  id_user integer,
  id_document integer,
  CONSTRAINT row_doc_comment_pkey PRIMARY KEY (id),
  CONSTRAINT row_doc_comment_core_user_fkey FOREIGN KEY (id_user)
      REFERENCES public.core_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT row_doc_comment_row_document FOREIGN KEY (id_document)
      REFERENCES public.row_document (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.row_doc_comment
  OWNER TO jakj;

CREATE TABLE public.row_doc_history
(
  id serial NOT NULL,
  file_version integer,
  date_creation timestamp with time zone,
  storage_identifier text,
  storage_callout_id integer,
  id_user integer,
  id_document integer,
  CONSTRAINT row_doc_history_pkey PRIMARY KEY (id),
  CONSTRAINT row_doc_history_core_user_fkey FOREIGN KEY (id_user)
      REFERENCES public.core_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT row_doc_history_row_document FOREIGN KEY (id_document)
      REFERENCES public.row_document (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.row_doc_history
  OWNER TO jakj;

INSERT INTO c_document_type (id, type_label) values (1,'PDF');
INSERT INTO c_document_type (id, type_label) values (2,'Image');
INSERT INTO c_document_type (id, type_label) values (3,'Document Word');
INSERT INTO c_document_type (id, type_label) values (4,'Feuille de calcul Excel');
