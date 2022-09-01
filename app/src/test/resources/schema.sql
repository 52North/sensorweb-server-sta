--
-- PostgreSQL database dump
--

-- Dumped from database version 12.8 (Debian 12.8-1.pgdg100+1)
-- Dumped by pg_dump version 12.10

-- Started on 2022-06-14 12:49:58 UTC

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;
SET default_tablespace = '';
SET default_table_access_method = heap;

--
-- TOC entry 208 (class 1259 OID 20707)
-- Name: category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category (
    category_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE public.category OWNER TO postgres;

--
-- TOC entry 4572 (class 0 OID 0)
-- Dependencies: 208
-- Name: TABLE category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.category IS 'Storage of the categories which should be used to group the data (e.g. grouping of phemomenon).';


--
-- TOC entry 4573 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN category.category_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category.category_id IS 'PK column of the table';


--
-- TOC entry 4574 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN category.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category.identifier IS 'Unique identifier of the category which can be used for filtering. Should be a URI, UUID. E.g. http://www.example.org/123, 123-321';


--
-- TOC entry 4575 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN category.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category.name IS 'The human readable name of the category.';


--
-- TOC entry 4576 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN category.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category.description IS 'A short description of the category';


--
-- TOC entry 209 (class 1259 OID 20715)
-- Name: category_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category_i18n (
    category_i18n_id bigint NOT NULL,
    fk_category_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE public.category_i18n OWNER TO postgres;

--
-- TOC entry 4577 (class 0 OID 0)
-- Dependencies: 209
-- Name: TABLE category_i18n; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.category_i18n IS 'Storage for internationalizations of categories.';


--
-- TOC entry 4578 (class 0 OID 0)
-- Dependencies: 209
-- Name: COLUMN category_i18n.category_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category_i18n.category_i18n_id IS 'PK column of the table';


--
-- TOC entry 4579 (class 0 OID 0)
-- Dependencies: 209
-- Name: COLUMN category_i18n.fk_category_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category_i18n.fk_category_id IS 'Reference to the category table this internationalization belongs to. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4580 (class 0 OID 0)
-- Dependencies: 209
-- Name: COLUMN category_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category_i18n.locale IS 'Locale/language specification for this entry';


--
-- TOC entry 4581 (class 0 OID 0)
-- Dependencies: 209
-- Name: COLUMN category_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category_i18n.name IS 'Locale/language specific name of the category';


--
-- TOC entry 4582 (class 0 OID 0)
-- Dependencies: 209
-- Name: COLUMN category_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.category_i18n.description IS 'Locale/language specific description of the category';


--
-- TOC entry 259 (class 1259 OID 21270)
-- Name: category_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.category_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.category_i18n_seq OWNER TO postgres;

--
-- TOC entry 260 (class 1259 OID 21272)
-- Name: category_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.category_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.category_seq OWNER TO postgres;

--
-- TOC entry 210 (class 1259 OID 20723)
-- Name: codespace; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.codespace (
    codespace_id bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.codespace OWNER TO postgres;

--
-- TOC entry 4583 (class 0 OID 0)
-- Dependencies: 210
-- Name: TABLE codespace; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.codespace IS 'Storage of codespaces which can be domain specific.';


--
-- TOC entry 4584 (class 0 OID 0)
-- Dependencies: 210
-- Name: COLUMN codespace.codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.codespace.codespace_id IS 'PK column of the table';


--
-- TOC entry 4585 (class 0 OID 0)
-- Dependencies: 210
-- Name: COLUMN codespace.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.codespace.name IS 'Name/definition of the codespace, e.g. of a domain';


--
-- TOC entry 261 (class 1259 OID 21274)
-- Name: codespace_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.codespace_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codespace_seq OWNER TO postgres;

--
-- TOC entry 211 (class 1259 OID 20728)
-- Name: composite_phenomenon; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.composite_phenomenon (
    fk_child_phenomenon_id bigint NOT NULL,
    fk_parent_phenomenon_id bigint NOT NULL
);


ALTER TABLE public.composite_phenomenon OWNER TO postgres;

--
-- TOC entry 4586 (class 0 OID 0)
-- Dependencies: 211
-- Name: TABLE composite_phenomenon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.composite_phenomenon IS 'Storage of hierarchies between phenomenon, e.g. for composite phenomenon like weather with temperature, windspeed, ...';


--
-- TOC entry 4587 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN composite_phenomenon.fk_child_phenomenon_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.composite_phenomenon.fk_child_phenomenon_id IS 'Reference to the child phenomenon in phenomenon table.';


--
-- TOC entry 4588 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN composite_phenomenon.fk_parent_phenomenon_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.composite_phenomenon.fk_parent_phenomenon_id IS 'Reference to the parent phenomenon in phenomenon table.';


--
-- TOC entry 212 (class 1259 OID 20733)
-- Name: dataset; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dataset (
    dataset_id bigint NOT NULL,
    discriminator character varying(255),
    identifier character varying(255),
    sta_identifier character varying(255),
    name character varying(255),
    description text,
    first_time timestamp without time zone,
    last_time timestamp without time zone,
    result_time_start timestamp without time zone,
    result_time_end timestamp without time zone,
    observed_area public.geometry,
    fk_procedure_id bigint NOT NULL,
    fk_phenomenon_id bigint NOT NULL,
    fk_offering_id bigint NOT NULL,
    fk_category_id bigint NOT NULL,
    fk_feature_id bigint,
    fk_platform_id bigint,
    fk_unit_id bigint,
    fk_format_id bigint,
    fk_aggregation_id bigint,
    first_value numeric(20,10),
    last_value numeric(20,10),
    fk_first_observation_id bigint,
    fk_last_observation_id bigint,
    dataset_type character varying(255) DEFAULT 'not_initialized'::character varying NOT NULL,
    observation_type character varying(255) DEFAULT 'not_initialized'::character varying NOT NULL,
    value_type character varying(255) DEFAULT 'not_initialized'::character varying NOT NULL,
    is_deleted smallint DEFAULT 0 NOT NULL,
    is_disabled smallint DEFAULT 0 NOT NULL,
    is_published smallint DEFAULT 1 NOT NULL,
    is_mobile smallint DEFAULT 0,
    is_insitu smallint DEFAULT 1,
    is_hidden smallint DEFAULT 0 NOT NULL,
    origin_timezone character varying(40),
    decimals integer,
    fk_identifier_codespace_id bigint,
    fk_name_codespace_id bigint,
    fk_value_profile_id bigint,
    CONSTRAINT dataset_dataset_type_check CHECK (((dataset_type)::text = ANY ((ARRAY['individualObservation'::character varying, 'sampling'::character varying, 'timeseries'::character varying, 'profile'::character varying, 'trajectory'::character varying, 'not_initialized'::character varying])::text[]))),
    CONSTRAINT dataset_is_deleted_check CHECK ((is_deleted = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_is_disabled_check CHECK ((is_disabled = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_is_hidden_check CHECK ((is_hidden = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_is_insitu_check CHECK ((is_insitu = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_is_mobile_check CHECK ((is_mobile = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_is_published_check CHECK ((is_published = ANY (ARRAY[1, 0]))),
    CONSTRAINT dataset_observation_type_check CHECK (((observation_type)::text = ANY ((ARRAY['simple'::character varying, 'profile'::character varying, 'timeseries'::character varying, 'trajectory'::character varying, 'not_initialized'::character varying])::text[]))),
    CONSTRAINT dataset_value_type_check CHECK (((value_type)::text = ANY ((ARRAY['quantity'::character varying, 'count'::character varying, 'text'::character varying, 'category'::character varying, 'bool'::character varying, 'geometry'::character varying, 'blob'::character varying, 'reference'::character varying, 'complex'::character varying, 'dataarray'::character varying, 'not_initialized'::character varying])::text[])))
);


ALTER TABLE public.dataset OWNER TO postgres;

--
-- TOC entry 4589 (class 0 OID 0)
-- Dependencies: 212
-- Name: TABLE dataset; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.dataset IS 'Storage of the dataset, the core table of the whole database model.';


--
-- TOC entry 4590 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.dataset_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.dataset_id IS 'PK column of the table';


--
-- TOC entry 4591 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.discriminator; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.discriminator IS 'Indicator used by Hibernate to distinguish between different types of datasets. Used e.g. for STA DatasetAggregations.';


--
-- TOC entry 4592 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.identifier IS 'Unique identifier of the dataset which can be used for filtering, e.g. GetObservationById in the SOS and can be encoded in WaterML 2.0 oder TimeseriesML 1.0 outputs.';


--
-- TOC entry 4593 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.sta_identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.sta_identifier IS 'Unique identifier used by SensorThingsAPI for addressing the entity. Should be a URI (reference to a vocabulary entry), UUID. E.g. 123, 123-321';


--
-- TOC entry 4594 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.name IS 'The human readable name of the dataset.';


--
-- TOC entry 4595 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.description IS 'A short description of the dataset';


--
-- TOC entry 4596 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.first_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.first_time IS 'The timestamp of the temporally first observation that belongs to this dataset.';


--
-- TOC entry 4597 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.last_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.last_time IS 'The timestamp of the temporally last observation that belongs to this dataset.';


--
-- TOC entry 4598 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.result_time_start; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.result_time_start IS 'The timestamp of the earliest result time of the observations that belong to this dataset.';


--
-- TOC entry 4599 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.result_time_end; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.result_time_end IS 'The timestamp of the latest result time of the observations that belong to this dataset.';


--
-- TOC entry 4600 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_procedure_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_procedure_id IS 'Reference to the procedure that belongs that belongs to this dataset.';


--
-- TOC entry 4601 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_phenomenon_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_phenomenon_id IS 'Reference to the phenomenon that belongs that belongs to this dataset.';


--
-- TOC entry 4602 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_offering_id IS 'Reference to the offering that belongs that belongs to this dataset.';


--
-- TOC entry 4603 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_category_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_category_id IS 'Reference to the category that belongs that belongs to this dataset.';


--
-- TOC entry 4604 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_feature_id IS 'Reference to the feature that belongs that belongs to this dataset.';


--
-- TOC entry 4605 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_platform_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_platform_id IS 'Reference to the platform that belongs that belongs to this dataset.';


--
-- TOC entry 4606 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_unit_id IS 'Reference to the unit of the observations that belongs to this dataset.';


--
-- TOC entry 4607 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_format_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_format_id IS 'Reference to the observationType in the format table. Required by the SOS to persist the valid observationType for the dataset.';


--
-- TOC entry 4608 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_aggregation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_aggregation_id IS 'Reference to the aggregation if this dataset belongs to one.';


--
-- TOC entry 4609 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.first_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.first_value IS 'The value of the temporally first observation that belongs to this dataset.';


--
-- TOC entry 4610 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.last_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.last_value IS 'The value of the temporally last quantity observation that belongs to this dataset.';


--
-- TOC entry 4611 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_first_observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_first_observation_id IS 'Reference to the temporally first observation in the observation table that belongs to this dataset.';


--
-- TOC entry 4612 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_last_observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_last_observation_id IS 'Reference to the temporally last observation in the observation table that belongs to this dataset.';


--
-- TOC entry 4613 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.dataset_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.dataset_type IS 'Indicator whether the dataset provides individualObservation (individual observations), timeseries (timeseries obervations) or trajectories (trajectory observations).';


--
-- TOC entry 4614 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.observation_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.observation_type IS 'Indicator whether the dataset observations are of type simple (a simple observation, e.g. a scalar value like the temperature) or profile (profile observations)';


--
-- TOC entry 4615 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.value_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.value_type IS 'Indicator of the type of the single values. Valid values are quantity (scalar values), count (integer values), text (textual values), category (categorical values), bool (boolean values), reference (references, e.g. link to a source, photo, video)';


--
-- TOC entry 4616 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.is_deleted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.is_deleted IS 'Flag that indicates if this dataset is deleted';


--
-- TOC entry 4617 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.is_disabled; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.is_disabled IS 'Flag that indicates if this dataset is disabled for insertion of new data';


--
-- TOC entry 4618 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.is_published; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.is_published IS 'Flag that indicates if this dataset should be published';


--
-- TOC entry 4619 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.is_mobile; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.is_mobile IS 'Flag that indicates if the procedure is mobile (1/true) or stationary (0/false).';


--
-- TOC entry 4620 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.is_insitu; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.is_insitu IS 'Flag that indicates if the procedure is insitu (1/true) or remote (0/false).';


--
-- TOC entry 4621 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.is_hidden; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.is_hidden IS 'Flag that indicates if this dataset should be hidden, e.g. for sub-datasets of a complex datasets';


--
-- TOC entry 4622 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.origin_timezone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.origin_timezone IS 'Define the origin timezone of the dataset timestamps. Possible values are offset (+02:00), id (CET) or full name (Europe/Berlin). It no time zone is defined, UTC would be used as default.';


--
-- TOC entry 4623 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.decimals; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.decimals IS 'Number of decimals that should be present in the output of the observation values. If no value is set, all decimals would be present.';


--
-- TOC entry 4624 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_identifier_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_identifier_codespace_id IS 'The codespace of the dataset identifier, reference to the codespace table. Can be null.';


--
-- TOC entry 4625 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_name_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_name_codespace_id IS 'The codespace of the dataset name, reference to the codespace table. Can be null.';


--
-- TOC entry 4626 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN dataset.fk_value_profile_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset.fk_value_profile_id IS 'Reference to the vertical metadata that belongs to this profile dataset.';


--
-- TOC entry 213 (class 1259 OID 20759)
-- Name: dataset_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dataset_i18n (
    dataset_i18n_id bigint NOT NULL,
    fk_dataset_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE public.dataset_i18n OWNER TO postgres;

--
-- TOC entry 4627 (class 0 OID 0)
-- Dependencies: 213
-- Name: COLUMN dataset_i18n.dataset_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_i18n.dataset_i18n_id IS 'PK column of the table';


--
-- TOC entry 4628 (class 0 OID 0)
-- Dependencies: 213
-- Name: COLUMN dataset_i18n.fk_dataset_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_i18n.fk_dataset_id IS 'Reference to the dataset table this internationalization belongs to.';


--
-- TOC entry 4629 (class 0 OID 0)
-- Dependencies: 213
-- Name: COLUMN dataset_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_i18n.locale IS 'Locale/language specification for this entry. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4630 (class 0 OID 0)
-- Dependencies: 213
-- Name: COLUMN dataset_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_i18n.name IS 'Locale/language specific name of the dataset entity';


--
-- TOC entry 4631 (class 0 OID 0)
-- Dependencies: 213
-- Name: COLUMN dataset_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_i18n.description IS 'Locale/language specific description of the dataset entity';


--
-- TOC entry 262 (class 1259 OID 21276)
-- Name: dataset_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dataset_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_i18n_seq OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 20767)
-- Name: dataset_parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dataset_parameter (
    parameter_id bigint NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    last_update timestamp without time zone,
    domain character varying(255),
    fk_dataset_id bigint NOT NULL,
    value_boolean smallint,
    value_category character varying(255),
    fk_unit_id bigint,
    value_count integer,
    value_quantity numeric(19,2),
    value_text character varying(255),
    value_xml text,
    value_json text,
    value_temporal_from timestamp without time zone,
    value_temporal_to timestamp without time zone,
    fk_parent_parameter_id bigint,
    CONSTRAINT dataset_parameter_type_check CHECK (((type)::text = ANY ((ARRAY['bool'::character varying, 'category'::character varying, 'count'::character varying, 'quantity'::character varying, 'text'::character varying, 'xml'::character varying, 'json'::character varying, 'complex'::character varying, 'temporal'::character varying])::text[])))
);


ALTER TABLE public.dataset_parameter OWNER TO postgres;

--
-- TOC entry 4632 (class 0 OID 0)
-- Dependencies: 214
-- Name: TABLE dataset_parameter; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.dataset_parameter IS 'Storage for additional information for platforms';


--
-- TOC entry 4633 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.parameter_id IS 'PK column of the table';


--
-- TOC entry 4634 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.type IS 'Indicator used by Hibernate to map value specific entities.';


--
-- TOC entry 4635 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.name IS 'The name of the parameter';


--
-- TOC entry 4636 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.description IS 'A short description of the parameter';


--
-- TOC entry 4637 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.last_update; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.last_update IS 'Timestamp that provides the time of the last modification of this entry';


--
-- TOC entry 4638 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.domain; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.domain IS 'The domain this parameter belongs to.';


--
-- TOC entry 4639 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.fk_dataset_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.fk_dataset_id IS 'Reference to the Dataset this Parameter describes.';


--
-- TOC entry 4640 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_boolean; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_boolean IS 'Storage of a boolean parameter value.';


--
-- TOC entry 4641 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_category IS 'Storage of a categorical parameter value.';


--
-- TOC entry 4642 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.fk_unit_id IS 'Reference to the unit of this value in the unit table';


--
-- TOC entry 4643 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_count IS 'Storage of a count parameter value.';


--
-- TOC entry 4644 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_quantity IS 'Storage of a quantity parameter value.';


--
-- TOC entry 4645 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_text IS 'Storage of a textual parameter value.';


--
-- TOC entry 4646 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_xml IS 'Storage of a XML encoded parameter value.';


--
-- TOC entry 4647 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_json IS 'Storage of a JSON encoded parameter value.';


--
-- TOC entry 4648 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_temporal_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_temporal_from IS 'Storage of a temporal from parameter value.';


--
-- TOC entry 4649 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.value_temporal_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.value_temporal_to IS 'Storage of a temporal to parameter value.';


--
-- TOC entry 4650 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN dataset_parameter.fk_parent_parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_parameter.fk_parent_parameter_id IS 'Reference to the parent parameter';


--
-- TOC entry 215 (class 1259 OID 20776)
-- Name: dataset_reference; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dataset_reference (
    fk_dataset_id_from bigint NOT NULL,
    sort_order integer NOT NULL,
    fk_dataset_id_to bigint NOT NULL
);


ALTER TABLE public.dataset_reference OWNER TO postgres;

--
-- TOC entry 4651 (class 0 OID 0)
-- Dependencies: 215
-- Name: TABLE dataset_reference; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.dataset_reference IS 'Storage of reference datasets, e.g. level zero, medium water level,etc. for water level.';


--
-- TOC entry 4652 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN dataset_reference.fk_dataset_id_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_reference.fk_dataset_id_from IS 'Reference to the dataset that has reference datasets';


--
-- TOC entry 4653 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN dataset_reference.sort_order; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_reference.sort_order IS 'Provides the sort order for the reference datasets.';


--
-- TOC entry 4654 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN dataset_reference.fk_dataset_id_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dataset_reference.fk_dataset_id_to IS 'Reference to the dataset that belongs to another dataset and provides values like level zero, medium water level,etc. for water level.';


--
-- TOC entry 263 (class 1259 OID 21278)
-- Name: dataset_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dataset_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dataset_seq OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 20781)
-- Name: feature; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feature (
    feature_id bigint NOT NULL,
    discriminator character varying(255),
    fk_format_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    sta_identifier character varying(255) NOT NULL,
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description text,
    xml text,
    url character varying(255),
    geom public.geometry
);


ALTER TABLE public.feature OWNER TO postgres;

--
-- TOC entry 4655 (class 0 OID 0)
-- Dependencies: 216
-- Name: TABLE feature; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.feature IS 'Storage of the features (OfInterest). A feature represents the observed location, route, or area. As examples, the location of the weather station or the water level location, a ferry (Cuxhaven-Helgoland) or a lake of interest.';


--
-- TOC entry 4656 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.feature_id IS 'PK column of the table';


--
-- TOC entry 4657 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.discriminator; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.discriminator IS 'Indicator used by Hibernate to map value specific entities (e.g. of a WaterML 2.0 MonitoringPoint) which are stored in separate tables.';


--
-- TOC entry 4658 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.fk_format_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.fk_format_id IS 'Reference to the featureType in the format table. Required by the SOS to identify the typ of the feature, e.g. http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint.';


--
-- TOC entry 4659 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.identifier IS 'Unique identifier of the feature which is used for filtering. Should be a URI, UUID. E.g. http://www.example.org/123, 123-321';


--
-- TOC entry 4660 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.sta_identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.sta_identifier IS 'Unique identifier used by SensorThingsAPI for addressing the entity. Should be a URI (reference to a vocabulary entry), UUID. E.g. 123, 123-321';


--
-- TOC entry 4661 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.fk_identifier_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.fk_identifier_codespace_id IS 'The codespace of the feature identifier, reference to the codespace table.';


--
-- TOC entry 4662 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.name IS 'The human readable name of the feature.';


--
-- TOC entry 4663 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.fk_name_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.fk_name_codespace_id IS 'The codespace of the feature name, reference to the codespace table.';


--
-- TOC entry 4664 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.description IS 'A short description of the feature';


--
-- TOC entry 4665 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.xml IS 'The XML encoded representation of the feature.';


--
-- TOC entry 4666 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.url IS 'Optional URL to an external resource that describes the feature, e.g. a WFS';


--
-- TOC entry 4667 (class 0 OID 0)
-- Dependencies: 216
-- Name: COLUMN feature.geom; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature.geom IS 'The geometry/location of feature';


--
-- TOC entry 217 (class 1259 OID 20789)
-- Name: feature_hierarchy; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feature_hierarchy (
    fk_child_feature_id bigint NOT NULL,
    fk_parent_feature_id bigint NOT NULL
);


ALTER TABLE public.feature_hierarchy OWNER TO postgres;

--
-- TOC entry 4668 (class 0 OID 0)
-- Dependencies: 217
-- Name: TABLE feature_hierarchy; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.feature_hierarchy IS 'Storage of hierarchies between features';


--
-- TOC entry 4669 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN feature_hierarchy.fk_child_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_hierarchy.fk_child_feature_id IS 'Reference to the child feature in feature table.';


--
-- TOC entry 4670 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN feature_hierarchy.fk_parent_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_hierarchy.fk_parent_feature_id IS 'Reference to the parent feature in feature table.';


--
-- TOC entry 218 (class 1259 OID 20794)
-- Name: feature_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feature_i18n (
    feature_i18n_id bigint NOT NULL,
    fk_feature_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE public.feature_i18n OWNER TO postgres;

--
-- TOC entry 4671 (class 0 OID 0)
-- Dependencies: 218
-- Name: TABLE feature_i18n; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.feature_i18n IS 'Storage for internationalizations of features.';


--
-- TOC entry 4672 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN feature_i18n.feature_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_i18n.feature_i18n_id IS 'PK column of the table';


--
-- TOC entry 4673 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN feature_i18n.fk_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_i18n.fk_feature_id IS 'Reference to the feature table this internationalization belongs to.';


--
-- TOC entry 4674 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN feature_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_i18n.locale IS 'Locale/language specification for this entry. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4675 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN feature_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_i18n.name IS 'Locale/language specific name of the feature';


--
-- TOC entry 4676 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN feature_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_i18n.description IS 'Locale/language specific description of the feature';


--
-- TOC entry 264 (class 1259 OID 21280)
-- Name: feature_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.feature_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.feature_i18n_seq OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 20802)
-- Name: feature_parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feature_parameter (
    parameter_id bigint NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    last_update timestamp without time zone,
    domain character varying(255),
    fk_feature_id bigint NOT NULL,
    value_boolean smallint,
    value_category character varying(255),
    fk_unit_id bigint,
    value_count integer,
    value_quantity numeric(19,2),
    value_text character varying(255),
    value_xml text,
    value_json text,
    value_temporal_from timestamp without time zone,
    value_temporal_to timestamp without time zone,
    fk_parent_parameter_id bigint,
    CONSTRAINT feature_parameter_type_check CHECK (((type)::text = ANY ((ARRAY['bool'::character varying, 'category'::character varying, 'count'::character varying, 'quantity'::character varying, 'text'::character varying, 'xml'::character varying, 'json'::character varying, 'complex'::character varying, 'temporal'::character varying])::text[])))
);


ALTER TABLE public.feature_parameter OWNER TO postgres;

--
-- TOC entry 4677 (class 0 OID 0)
-- Dependencies: 219
-- Name: TABLE feature_parameter; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.feature_parameter IS 'Storage for additional information for platforms';


--
-- TOC entry 4678 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.parameter_id IS 'PK column of the table';


--
-- TOC entry 4679 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.type IS 'Indicator used by Hibernate to map value specific entities.';


--
-- TOC entry 4680 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.name IS 'The name of the parameter';


--
-- TOC entry 4681 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.description IS 'A short description of the parameter';


--
-- TOC entry 4682 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.last_update; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.last_update IS 'Timestamp that provides the time of the last modification of this entry';


--
-- TOC entry 4683 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.domain; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.domain IS 'The domain this parameter belongs to.';


--
-- TOC entry 4684 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.fk_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.fk_feature_id IS 'Reference to the Feature this Parameter describes.';


--
-- TOC entry 4685 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_boolean; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_boolean IS 'Storage of a boolean parameter value.';


--
-- TOC entry 4686 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_category IS 'Storage of a categorical parameter value.';


--
-- TOC entry 4687 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.fk_unit_id IS 'Reference to the unit of this value in the unit table';


--
-- TOC entry 4688 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_count IS 'Storage of a count parameter value.';


--
-- TOC entry 4689 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_quantity IS 'Storage of a quantity parameter value.';


--
-- TOC entry 4690 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_text IS 'Storage of a textual parameter value.';


--
-- TOC entry 4691 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_xml IS 'Storage of a XML encoded parameter value.';


--
-- TOC entry 4692 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_json IS 'Storage of a JSON encoded parameter value.';


--
-- TOC entry 4693 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_temporal_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_temporal_from IS 'Storage of a temporal from parameter value.';


--
-- TOC entry 4694 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.value_temporal_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.value_temporal_to IS 'Storage of a temporal to parameter value.';


--
-- TOC entry 4695 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN feature_parameter.fk_parent_parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.feature_parameter.fk_parent_parameter_id IS 'Reference to the parent parameter';


--
-- TOC entry 265 (class 1259 OID 21282)
-- Name: feature_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.feature_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.feature_seq OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 20811)
-- Name: format; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.format (
    format_id bigint NOT NULL,
    definition character varying(255) NOT NULL
);


ALTER TABLE public.format OWNER TO postgres;

--
-- TOC entry 4696 (class 0 OID 0)
-- Dependencies: 220
-- Name: TABLE format; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.format IS 'Storage of types (feature, observation) and formats (procedure)., e.g. http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement and http://www.opengis.net/sensorml/2.0';


--
-- TOC entry 4697 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN format.format_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.format.format_id IS 'PK column of the table';


--
-- TOC entry 4698 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN format.definition; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.format.definition IS 'The definition of the format.';


--
-- TOC entry 266 (class 1259 OID 21284)
-- Name: format_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.format_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.format_seq OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 20816)
-- Name: historical_location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.historical_location (
    historical_location_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    sta_identifier character varying(255) NOT NULL,
    fk_platform_id bigint NOT NULL,
    "time" timestamp without time zone NOT NULL
);


ALTER TABLE public.historical_location OWNER TO postgres;

--
-- TOC entry 4699 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN historical_location.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.historical_location.identifier IS 'Unique identifier of the HistoricalLocation. Should be a URI, UUID. E.g. http://www.example.org/123, 123-321.';


--
-- TOC entry 4700 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN historical_location.sta_identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.historical_location.sta_identifier IS 'Unique identifier used by SensorThingsAPI for addressing the entity. Should be a URI (reference to a vocabulary entry), UUID. E.g. 123, 123-321';


--
-- TOC entry 267 (class 1259 OID 21286)
-- Name: historical_location_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.historical_location_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.historical_location_seq OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 20824)
-- Name: location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.location (
    location_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    sta_identifier character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text NOT NULL,
    location text,
    geom public.geometry,
    fk_format_id bigint NOT NULL
);


ALTER TABLE public.location OWNER TO postgres;

--
-- TOC entry 4701 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN location.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location.identifier IS 'Unique identifier of the location. Should be a URI, UUID. E.g. http://www.example.org/123, 123-321.';


--
-- TOC entry 4702 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN location.sta_identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location.sta_identifier IS 'Unique identifier used by SensorThingsAPI for addressing the entity. Should be a URI (reference to a vocabulary entry), UUID. E.g. 123, 123-321';


--
-- TOC entry 223 (class 1259 OID 20832)
-- Name: location_historical_location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.location_historical_location (
    fk_location_id bigint NOT NULL,
    fk_historical_location_id bigint NOT NULL
);


ALTER TABLE public.location_historical_location OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 20837)
-- Name: location_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.location_i18n (
    location_i18n_id bigint NOT NULL,
    fk_location_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text,
    location text
);


ALTER TABLE public.location_i18n OWNER TO postgres;

--
-- TOC entry 4703 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN location_i18n.location_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_i18n.location_i18n_id IS 'PK column of the table';


--
-- TOC entry 4704 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN location_i18n.fk_location_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_i18n.fk_location_id IS 'Reference to the feature table this internationalization belongs to.';


--
-- TOC entry 4705 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN location_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_i18n.locale IS 'Locale/language specification for this entry';


--
-- TOC entry 4706 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN location_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_i18n.name IS 'Locale/language specific name of the location';


--
-- TOC entry 4707 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN location_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_i18n.description IS 'Locale/language specific description of the location';


--
-- TOC entry 4708 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN location_i18n.location; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_i18n.location IS 'Locale/language specific location property of the location';


--
-- TOC entry 268 (class 1259 OID 21288)
-- Name: location_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.location_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.location_i18n_seq OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 20845)
-- Name: location_parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.location_parameter (
    parameter_id bigint NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    last_update timestamp without time zone,
    domain character varying(255),
    fk_location_id bigint NOT NULL,
    value_boolean smallint,
    value_category character varying(255),
    fk_unit_id bigint,
    value_count integer,
    value_quantity numeric(19,2),
    value_text character varying(255),
    value_xml text,
    value_json text,
    value_temporal_from timestamp without time zone,
    value_temporal_to timestamp without time zone,
    fk_parent_parameter_id bigint,
    CONSTRAINT location_parameter_type_check CHECK (((type)::text = ANY ((ARRAY['bool'::character varying, 'category'::character varying, 'count'::character varying, 'quantity'::character varying, 'text'::character varying, 'xml'::character varying, 'json'::character varying, 'complex'::character varying, 'temporal'::character varying])::text[])))
);


ALTER TABLE public.location_parameter OWNER TO postgres;

--
-- TOC entry 4709 (class 0 OID 0)
-- Dependencies: 225
-- Name: TABLE location_parameter; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.location_parameter IS 'Storage for additional information for platforms';


--
-- TOC entry 4710 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.parameter_id IS 'PK column of the table';


--
-- TOC entry 4711 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.type IS 'Indicator used by Hibernate to map value specific entities.';


--
-- TOC entry 4712 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.name IS 'The name of the parameter';


--
-- TOC entry 4713 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.description IS 'A short description of the parameter';


--
-- TOC entry 4714 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.last_update; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.last_update IS 'Timestamp that provides the time of the last modification of this entry';


--
-- TOC entry 4715 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.domain; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.domain IS 'The domain this parameter belongs to.';


--
-- TOC entry 4716 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.fk_location_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.fk_location_id IS 'Reference to the Location this Parameter describes.';


--
-- TOC entry 4717 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_boolean; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_boolean IS 'Storage of a boolean parameter value.';


--
-- TOC entry 4718 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_category IS 'Storage of a categorical parameter value.';


--
-- TOC entry 4719 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.fk_unit_id IS 'Reference to the unit of this value in the unit table';


--
-- TOC entry 4720 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_count IS 'Storage of a count parameter value.';


--
-- TOC entry 4721 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_quantity IS 'Storage of a quantity parameter value.';


--
-- TOC entry 4722 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_text IS 'Storage of a textual parameter value.';


--
-- TOC entry 4723 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_xml IS 'Storage of a XML encoded parameter value.';


--
-- TOC entry 4724 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_json IS 'Storage of a JSON encoded parameter value.';


--
-- TOC entry 4725 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_temporal_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_temporal_from IS 'Storage of a temporal from parameter value.';


--
-- TOC entry 4726 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.value_temporal_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.value_temporal_to IS 'Storage of a temporal to parameter value.';


--
-- TOC entry 4727 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN location_parameter.fk_parent_parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.location_parameter.fk_parent_parameter_id IS 'Reference to the parent parameter';


--
-- TOC entry 269 (class 1259 OID 21290)
-- Name: location_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.location_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.location_seq OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 20854)
-- Name: observation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.observation (
    observation_id bigint NOT NULL,
    value_type character varying(255) NOT NULL,
    fk_dataset_id bigint NOT NULL,
    sampling_time_start timestamp without time zone NOT NULL,
    sampling_time_end timestamp without time zone NOT NULL,
    result_time timestamp without time zone,
    identifier character varying(255),
    sta_identifier character varying(255) NOT NULL,
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description text,
    is_deleted smallint DEFAULT 0 NOT NULL,
    valid_time_start timestamp without time zone,
    valid_time_end timestamp without time zone,
    sampling_geometry public.geometry,
    value_identifier character varying(255),
    value_name character varying(255),
    value_description character varying(255),
    vertical_from numeric(20,10) DEFAULT 0 NOT NULL,
    vertical_to numeric(20,10) DEFAULT 0 NOT NULL,
    fk_parent_observation_id bigint,
    value_quantity numeric(20,10),
    value_text character varying(255),
    value_count integer,
    value_category character varying(255),
    value_boolean smallint,
    detection_limit_flag smallint,
    detection_limit numeric(20,10),
    value_reference character varying(255),
    value_geometry public.geometry,
    value_array text,
    fk_result_template_id bigint,
    CONSTRAINT observation_detection_limit_flag_check CHECK ((detection_limit_flag = ANY (ARRAY[NULL::integer, '-1'::integer, 1]))),
    CONSTRAINT observation_is_deleted_check CHECK ((is_deleted = ANY (ARRAY[1, 0]))),
    CONSTRAINT observation_value_type_check CHECK (((value_type)::text = ANY ((ARRAY['quantity'::character varying, 'count'::character varying, 'text'::character varying, 'category'::character varying, 'bool'::character varying, 'profile'::character varying, 'complex'::character varying, 'dataarray'::character varying, 'geometry'::character varying, 'blob'::character varying, 'reference'::character varying, 'trajectory'::character varying])::text[])))
);


ALTER TABLE public.observation OWNER TO postgres;

--
-- TOC entry 4728 (class 0 OID 0)
-- Dependencies: 226
-- Name: TABLE observation; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.observation IS 'Storage of the observation values with the timestamp and additional metadata. The metadata are height/depth values for profile observation and sampling geometries for trajectory observations. In each observation entry only one value_... column should be filled with a value!';


--
-- TOC entry 4729 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.observation_id IS 'PK column of the table';


--
-- TOC entry 4730 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.value_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.value_type IS 'Indicator used by Hibernate to map value specific entities. Valid values are quantity (scalar values in value_quantity), count (integer values in value_count), text (textual values in value_text), category (categorical values in value_category), bool (boolean values in value_boolean), reference (references in value_reference, e.g. link to a source, photo, video)';


--
-- TOC entry 4731 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.fk_dataset_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.fk_dataset_id IS 'Reference to the dataset to which this observation belongs.';


--
-- TOC entry 4732 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.sampling_time_start; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.sampling_time_start IS 'The timestamp when the observation period has started or the observation took place. In the the latter, sampling_time_start and sampling_time_end are equal.';


--
-- TOC entry 4733 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.sampling_time_end; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.sampling_time_end IS 'The timestamp when the measurement period has finished or the observation took place. In the the latter, sampling_time_start and sampling_time_end are equal.';


--
-- TOC entry 4734 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.result_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.result_time IS 'The timestamp when the observation was published. Might be identical with sampling_time_start and sampling_time_end.';


--
-- TOC entry 4735 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.identifier IS 'Unique identifier of the observation which can be for used filtering, e.g. GetObservationById in the SOS. Should be a URI, UUID. E.g. http://www.example.org/123, 123-321';


--
-- TOC entry 4736 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.sta_identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.sta_identifier IS 'Unique identifier used by SensorThingsAPI for addressing the entity. Should be a URI (reference to a vocabulary entry), UUID. E.g. 123, 123-321';


--
-- TOC entry 4737 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.fk_identifier_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.fk_identifier_codespace_id IS 'The codespace of the data/observation identifier, reference to the codespace table. Can be null.';


--
-- TOC entry 4738 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.name IS 'The human readable name of the observation.';


--
-- TOC entry 4739 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.fk_name_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.fk_name_codespace_id IS 'The codespace of the data/observation name, reference to the codespace table. Can be null.';


--
-- TOC entry 4740 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.description IS 'A short description of the observation';


--
-- TOC entry 4741 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.is_deleted; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.is_deleted IS 'Flag that indicates if this observation is deleted';


--
-- TOC entry 4742 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.valid_time_start; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.valid_time_start IS 'The timestamp from when the obervation is valid, e.g. forcaste observations';


--
-- TOC entry 4743 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.valid_time_end; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.valid_time_end IS 'The timestamp until when the obervation is valid, e.g. forcaste observations';


--
-- TOC entry 4744 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.sampling_geometry; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.sampling_geometry IS 'The geometry that represents the location where the observation was observed, e.g. mobile observations (trajectories) where this geometry is different from the feature geometry.';


--
-- TOC entry 4745 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.value_identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.value_identifier IS 'Identifier of the value. E.g. used in OGC SWE encoded values like SweText';


--
-- TOC entry 4746 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.value_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.value_name IS 'Identifier of the name. E.g. used in OGC SWE encoded values like SweText';


--
-- TOC entry 4747 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.value_description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.value_description IS 'Identifier of the description. E.g. used in OGC SWE encoded values like SweText';


--
-- TOC entry 4748 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.vertical_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.vertical_from IS 'The start level of a vertical observation, required for profile observations';


--
-- TOC entry 4749 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.vertical_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.vertical_to IS 'The end level or the level of a vertical observation, required for profile observations';


--
-- TOC entry 4750 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.fk_parent_observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.fk_parent_observation_id IS 'Reference to the parent observation in the case of complex observations like profiles, complex or swedataarray observations.';


--
-- TOC entry 4751 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.detection_limit_flag; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.detection_limit_flag IS 'Flag that indicates if measured value lower/higher of the detection limit.';


--
-- TOC entry 4752 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.detection_limit; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.detection_limit IS 'The detection limit';


--
-- TOC entry 4753 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.value_reference; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.value_reference IS 'The reference value (URI) of an observation (ReferenceObservation)';


--
-- TOC entry 4754 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.value_geometry; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.value_geometry IS 'The geometry value of an observation (GeometryObservation)';


--
-- TOC entry 4755 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.value_array; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.value_array IS 'The textual value of an observation (SweDataArrayObservation))';


--
-- TOC entry 4756 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN observation.fk_result_template_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation.fk_result_template_id IS 'Reference to the result template which holds the structure and encoding.';


--
-- TOC entry 227 (class 1259 OID 20868)
-- Name: observation_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.observation_i18n (
    observation_i18n_id bigint NOT NULL,
    fk_observation_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text,
    value_name character varying(255),
    value_description character varying(255)
);


ALTER TABLE public.observation_i18n OWNER TO postgres;

--
-- TOC entry 4757 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN observation_i18n.observation_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_i18n.observation_i18n_id IS 'PK column of the table';


--
-- TOC entry 4758 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN observation_i18n.fk_observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_i18n.fk_observation_id IS 'Reference to the data table this internationalization belongs to.';


--
-- TOC entry 4759 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN observation_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_i18n.locale IS 'Locale/language specification for this entry. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4760 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN observation_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_i18n.name IS 'Locale/language specific name of the data entity';


--
-- TOC entry 4761 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN observation_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_i18n.description IS 'Locale/language specific description of the data entity';


--
-- TOC entry 4762 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN observation_i18n.value_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_i18n.value_name IS 'Locale/language specific name of the data entity';


--
-- TOC entry 4763 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN observation_i18n.value_description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_i18n.value_description IS 'Locale/language specific description of the data entity';


--
-- TOC entry 270 (class 1259 OID 21292)
-- Name: observation_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.observation_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.observation_i18n_seq OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 20876)
-- Name: observation_parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.observation_parameter (
    parameter_id bigint NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    last_update timestamp without time zone,
    domain character varying(255),
    fk_observation_id bigint NOT NULL,
    value_boolean smallint,
    value_category character varying(255),
    fk_unit_id bigint,
    value_count integer,
    value_quantity numeric(19,2),
    value_text character varying(255),
    value_xml text,
    value_json text,
    value_temporal_from timestamp without time zone,
    value_temporal_to timestamp without time zone,
    fk_parent_parameter_id bigint,
    CONSTRAINT observation_parameter_type_check CHECK (((type)::text = ANY ((ARRAY['bool'::character varying, 'category'::character varying, 'count'::character varying, 'quantity'::character varying, 'text'::character varying, 'xml'::character varying, 'json'::character varying, 'complex'::character varying, 'temporal'::character varying])::text[])))
);


ALTER TABLE public.observation_parameter OWNER TO postgres;

--
-- TOC entry 4764 (class 0 OID 0)
-- Dependencies: 228
-- Name: TABLE observation_parameter; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.observation_parameter IS 'Storage of relations between observation and related parameter';


--
-- TOC entry 4765 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.parameter_id IS 'PK column of the table';


--
-- TOC entry 4766 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.type IS 'Indicator used by Hibernate to map value specific entities.';


--
-- TOC entry 4767 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.name IS 'The name of the parameter';


--
-- TOC entry 4768 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.description IS 'A short description of the parameter';


--
-- TOC entry 4769 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.last_update; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.last_update IS 'Timestamp that provides the time of the last modification of this entry';


--
-- TOC entry 4770 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.domain; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.domain IS 'The domain this parameter belongs to.';


--
-- TOC entry 4771 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.fk_observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.fk_observation_id IS 'Reference to the Observation this Parameter describes.';


--
-- TOC entry 4772 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_boolean; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_boolean IS 'Storage of a boolean parameter value.';


--
-- TOC entry 4773 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_category IS 'Storage of a categorical parameter value.';


--
-- TOC entry 4774 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.fk_unit_id IS 'Reference to the unit of this value in the unit table';


--
-- TOC entry 4775 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_count IS 'Storage of a count parameter value.';


--
-- TOC entry 4776 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_quantity IS 'Storage of a quantity parameter value.';


--
-- TOC entry 4777 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_text IS 'Storage of a textual parameter value.';


--
-- TOC entry 4778 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_xml IS 'Storage of a XML encoded parameter value.';


--
-- TOC entry 4779 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_json IS 'Storage of a JSON encoded parameter value.';


--
-- TOC entry 4780 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_temporal_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_temporal_from IS 'Storage of a temporal from parameter value.';


--
-- TOC entry 4781 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.value_temporal_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.value_temporal_to IS 'Storage of a temporal to parameter value.';


--
-- TOC entry 4782 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN observation_parameter.fk_parent_parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.observation_parameter.fk_parent_parameter_id IS 'Reference to the parent parameter';


--
-- TOC entry 271 (class 1259 OID 21294)
-- Name: observation_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.observation_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.observation_seq OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 20885)
-- Name: offering; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.offering (
    offering_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description text,
    sampling_time_start timestamp without time zone,
    sampling_time_end timestamp without time zone,
    result_time_start timestamp without time zone,
    result_time_end timestamp without time zone,
    valid_time_start timestamp without time zone,
    valid_time_end timestamp without time zone,
    geom public.geometry
);


ALTER TABLE public.offering OWNER TO postgres;

--
-- TOC entry 4783 (class 0 OID 0)
-- Dependencies: 229
-- Name: TABLE offering; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.offering IS 'Storage of the offerings which is required by the SOS. An offering is used in SOS to group records according to specific criteria. In the INSPIRE context, an offering is an "INSPRE spatial dataset," an identifiable collection of spatial data.';


--
-- TOC entry 4784 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.offering_id IS 'PK column of the table';


--
-- TOC entry 4785 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.identifier IS 'Unique identifier of the offering which can be used for filtering. Should be a URI, UUID. E.g. http://www.example.org/123, 123-321.';


--
-- TOC entry 4786 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.fk_identifier_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.fk_identifier_codespace_id IS 'The codespace of the offering identifier, reference to the codespace table.';


--
-- TOC entry 4787 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.name IS 'The human readable name of the offering.';


--
-- TOC entry 4788 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.fk_name_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.fk_name_codespace_id IS 'The codespace of the offering name, reference to the codespace table.';


--
-- TOC entry 4789 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.description IS 'A short description of the offering';


--
-- TOC entry 4790 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.sampling_time_start; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.sampling_time_start IS 'The minimum samplingTimeStart of all observation that belong to this offering. If the column is empty, the information is calculated during the cache update and stored locally. Used for the capabilities of the SOS.';


--
-- TOC entry 4791 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.sampling_time_end; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.sampling_time_end IS 'The maximum samplingTimeStart of all observation that belong to this offering. If the column is empty, the information is calculated during the cache update and stored locally. Used for the capabilities of the SOS.';


--
-- TOC entry 4792 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.result_time_start; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.result_time_start IS 'The minimum resultTimeStart of all observation that belong to this offering. If the column is empty, the information is calculated during the cache update and stored locally. Used for the capabilities of the SOS.';


--
-- TOC entry 4793 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.result_time_end; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.result_time_end IS 'The maximum resultTimeEnd of all observation that belong to this offering. If the column is empty, the information is calculated during the cache update and stored locally. Used for the capabilities of the SOS.';


--
-- TOC entry 4794 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.valid_time_start; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.valid_time_start IS 'The minimum validTimeStart of all observation that belong to this offering. If the column is empty, the information is calculated during the cache update and stored locally. Used for the capabilities of the SOS.';


--
-- TOC entry 4795 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.valid_time_end; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.valid_time_end IS 'The maximum validTimeEnd of all observation that belong to this offering. If the column is empty, the information is calculated during the cache update and stored locally. Used for the capabilities of the SOS.';


--
-- TOC entry 4796 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN offering.geom; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering.geom IS 'The envelope/geometry of all features or samplingGeometries of observations that belong to this offering. If the column is empty, the information is calculated during the cache update and stored locally. Used for the capabilities of the SOS.';


--
-- TOC entry 230 (class 1259 OID 20893)
-- Name: offering_feature_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.offering_feature_type (
    fk_offering_id bigint NOT NULL,
    fk_format_id bigint NOT NULL
);


ALTER TABLE public.offering_feature_type OWNER TO postgres;

--
-- TOC entry 4797 (class 0 OID 0)
-- Dependencies: 230
-- Name: TABLE offering_feature_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.offering_feature_type IS 'Relation to store the valid  featureTypes for the offering';


--
-- TOC entry 4798 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN offering_feature_type.fk_offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_feature_type.fk_offering_id IS 'The related offering';


--
-- TOC entry 4799 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN offering_feature_type.fk_format_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_feature_type.fk_format_id IS 'The reference of the related featureType from the format table';


--
-- TOC entry 231 (class 1259 OID 20898)
-- Name: offering_hierarchy; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.offering_hierarchy (
    fk_child_offering_id bigint NOT NULL,
    fk_parent_offering_id bigint NOT NULL
);


ALTER TABLE public.offering_hierarchy OWNER TO postgres;

--
-- TOC entry 4800 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN offering_hierarchy.fk_child_offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_hierarchy.fk_child_offering_id IS 'Reference to the child offering in offering table.';


--
-- TOC entry 4801 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN offering_hierarchy.fk_parent_offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_hierarchy.fk_parent_offering_id IS 'Reference to the parent offering in offering table.';


--
-- TOC entry 232 (class 1259 OID 20903)
-- Name: offering_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.offering_i18n (
    offering_i18n_id bigint NOT NULL,
    fk_offering_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE public.offering_i18n OWNER TO postgres;

--
-- TOC entry 4802 (class 0 OID 0)
-- Dependencies: 232
-- Name: TABLE offering_i18n; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.offering_i18n IS 'Storage for internationalizations of offerings.';


--
-- TOC entry 4803 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN offering_i18n.offering_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_i18n.offering_i18n_id IS 'PK column of the table';


--
-- TOC entry 4804 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN offering_i18n.fk_offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_i18n.fk_offering_id IS 'Reference to the offering table this internationalization belongs to.';


--
-- TOC entry 4805 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN offering_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_i18n.locale IS 'Locale/language specification for this entry. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4806 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN offering_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_i18n.name IS 'Locale/language specific name of the offering';


--
-- TOC entry 4807 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN offering_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_i18n.description IS 'Locale/language specific description of the offering';


--
-- TOC entry 272 (class 1259 OID 21296)
-- Name: offering_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.offering_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.offering_i18n_seq OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 20911)
-- Name: offering_observation_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.offering_observation_type (
    fk_offering_id bigint NOT NULL,
    fk_format_id bigint NOT NULL
);


ALTER TABLE public.offering_observation_type OWNER TO postgres;

--
-- TOC entry 4808 (class 0 OID 0)
-- Dependencies: 233
-- Name: TABLE offering_observation_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.offering_observation_type IS 'Relation to store the valid observationTypes for the offering';


--
-- TOC entry 4809 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN offering_observation_type.fk_offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_observation_type.fk_offering_id IS 'The related offering';


--
-- TOC entry 4810 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN offering_observation_type.fk_format_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_observation_type.fk_format_id IS 'The reference of the related observationType from the format table';


--
-- TOC entry 234 (class 1259 OID 20916)
-- Name: offering_related_feature; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.offering_related_feature (
    fk_offering_id bigint NOT NULL,
    fk_related_feature_id bigint NOT NULL
);


ALTER TABLE public.offering_related_feature OWNER TO postgres;

--
-- TOC entry 4811 (class 0 OID 0)
-- Dependencies: 234
-- Name: TABLE offering_related_feature; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.offering_related_feature IS 'Storage tfor the relation between offering and relatedFeature';


--
-- TOC entry 4812 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN offering_related_feature.fk_offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_related_feature.fk_offering_id IS 'The related offering';


--
-- TOC entry 4813 (class 0 OID 0)
-- Dependencies: 234
-- Name: COLUMN offering_related_feature.fk_related_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.offering_related_feature.fk_related_feature_id IS 'The reference to the relatedFeature from the relatedFeature table';


--
-- TOC entry 273 (class 1259 OID 21298)
-- Name: offering_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.offering_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.offering_seq OWNER TO postgres;

--
-- TOC entry 274 (class 1259 OID 21300)
-- Name: parameter_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.parameter_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parameter_seq OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 20921)
-- Name: phenomenon; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.phenomenon (
    phenomenon_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    sta_identifier character varying(255) NOT NULL,
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description text
);


ALTER TABLE public.phenomenon OWNER TO postgres;

--
-- TOC entry 4814 (class 0 OID 0)
-- Dependencies: 235
-- Name: TABLE phenomenon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.phenomenon IS 'Storage of the phenomenon/observableProperties, e.g. air temperature, water temperature, ...';


--
-- TOC entry 4815 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN phenomenon.phenomenon_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon.phenomenon_id IS 'PK column of the table';


--
-- TOC entry 4816 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN phenomenon.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon.identifier IS 'Unique identifier of the phenomenon which can be used for filtering. Should be a URI (reference to a vocabulary entry), UUID. E.g. http://www.example.org/123, 123-321';


--
-- TOC entry 4817 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN phenomenon.sta_identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon.sta_identifier IS 'Unique identifier used by SensorThingsAPI for addressing the entity. Should be a URI (reference to a vocabulary entry), UUID. E.g. 123, 123-321';


--
-- TOC entry 4818 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN phenomenon.fk_identifier_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon.fk_identifier_codespace_id IS 'The codespace of the phenomenon identifier, reference to the codespace table.';


--
-- TOC entry 4819 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN phenomenon.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon.name IS 'The human readable name of the phenomenon.';


--
-- TOC entry 4820 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN phenomenon.fk_name_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon.fk_name_codespace_id IS 'The codespace of the phenomenon name, reference to the codespace table.';


--
-- TOC entry 4821 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN phenomenon.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon.description IS 'A short description of the phenomenon';


--
-- TOC entry 236 (class 1259 OID 20929)
-- Name: phenomenon_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.phenomenon_i18n (
    phenomenon_i18n_id bigint NOT NULL,
    fk_phenomenon_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE public.phenomenon_i18n OWNER TO postgres;

--
-- TOC entry 4822 (class 0 OID 0)
-- Dependencies: 236
-- Name: TABLE phenomenon_i18n; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.phenomenon_i18n IS 'Storage for internationalizations of phenomenon.';


--
-- TOC entry 4823 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN phenomenon_i18n.phenomenon_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_i18n.phenomenon_i18n_id IS 'PK column of the table';


--
-- TOC entry 4824 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN phenomenon_i18n.fk_phenomenon_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_i18n.fk_phenomenon_id IS 'Reference to the phenomenon table this internationalization belongs to.';


--
-- TOC entry 4825 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN phenomenon_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_i18n.locale IS 'Locale/language specification for this entry. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4826 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN phenomenon_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_i18n.name IS 'Locale/language specific name of the phenomenon';


--
-- TOC entry 4827 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN phenomenon_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_i18n.description IS 'Locale/language specific description of the phenomenon';


--
-- TOC entry 275 (class 1259 OID 21302)
-- Name: phenomenon_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.phenomenon_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.phenomenon_i18n_seq OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 20937)
-- Name: phenomenon_parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.phenomenon_parameter (
    parameter_id bigint NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    last_update timestamp without time zone,
    domain character varying(255),
    fk_phenomenon_id bigint NOT NULL,
    value_boolean smallint,
    value_category character varying(255),
    fk_unit_id bigint,
    value_count integer,
    value_quantity numeric(19,2),
    value_text character varying(255),
    value_xml text,
    value_json text,
    value_temporal_from timestamp without time zone,
    value_temporal_to timestamp without time zone,
    fk_parent_parameter_id bigint,
    CONSTRAINT phenomenon_parameter_type_check CHECK (((type)::text = ANY ((ARRAY['bool'::character varying, 'category'::character varying, 'count'::character varying, 'quantity'::character varying, 'text'::character varying, 'xml'::character varying, 'json'::character varying, 'complex'::character varying, 'temporal'::character varying])::text[])))
);


ALTER TABLE public.phenomenon_parameter OWNER TO postgres;

--
-- TOC entry 4828 (class 0 OID 0)
-- Dependencies: 237
-- Name: TABLE phenomenon_parameter; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.phenomenon_parameter IS 'Storage for additional information for platforms';


--
-- TOC entry 4829 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.parameter_id IS 'PK column of the table';


--
-- TOC entry 4830 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.type IS 'Indicator used by Hibernate to map value specific entities.';


--
-- TOC entry 4831 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.name IS 'The name of the parameter';


--
-- TOC entry 4832 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.description IS 'A short description of the parameter';


--
-- TOC entry 4833 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.last_update; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.last_update IS 'Timestamp that provides the time of the last modification of this entry';


--
-- TOC entry 4834 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.domain; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.domain IS 'The domain this parameter belongs to.';


--
-- TOC entry 4835 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.fk_phenomenon_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.fk_phenomenon_id IS 'Reference to the Phenomenon this Parameter describes.';


--
-- TOC entry 4836 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_boolean; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_boolean IS 'Storage of a boolean parameter value.';


--
-- TOC entry 4837 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_category IS 'Storage of a categorical parameter value.';


--
-- TOC entry 4838 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.fk_unit_id IS 'Reference to the unit of this value in the unit table';


--
-- TOC entry 4839 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_count IS 'Storage of a count parameter value.';


--
-- TOC entry 4840 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_quantity IS 'Storage of a quantity parameter value.';


--
-- TOC entry 4841 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_text IS 'Storage of a textual parameter value.';


--
-- TOC entry 4842 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_xml IS 'Storage of a XML encoded parameter value.';


--
-- TOC entry 4843 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_json IS 'Storage of a JSON encoded parameter value.';


--
-- TOC entry 4844 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_temporal_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_temporal_from IS 'Storage of a temporal from parameter value.';


--
-- TOC entry 4845 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.value_temporal_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.value_temporal_to IS 'Storage of a temporal to parameter value.';


--
-- TOC entry 4846 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN phenomenon_parameter.fk_parent_parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.phenomenon_parameter.fk_parent_parameter_id IS 'Reference to the parent parameter';


--
-- TOC entry 276 (class 1259 OID 21304)
-- Name: phenomenon_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.phenomenon_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.phenomenon_seq OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 20946)
-- Name: platform; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.platform (
    platform_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    sta_identifier character varying(255) NOT NULL,
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description text
);


ALTER TABLE public.platform OWNER TO postgres;

--
-- TOC entry 4847 (class 0 OID 0)
-- Dependencies: 238
-- Name: TABLE platform; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.platform IS 'Storage of the platforms. With a platform several procedures can be grouped or in the case of citizen science the platform can be the camera or mobile phone. An example of a platform is a vessel that has multiple sensors (Procedure). In most cases, the platform is the same as the platform or procedure, such as a weather station or a water level location.';


--
-- TOC entry 4848 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN platform.platform_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform.platform_id IS 'PK column of the table';


--
-- TOC entry 4849 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN platform.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform.identifier IS 'Unique identifier of the platform which can be used for filtering. Should be a URI, UUID. E.g. http://www.example.org/123, 123-321';


--
-- TOC entry 4850 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN platform.sta_identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform.sta_identifier IS 'Unique identifier used by SensorThingsAPI for addressing the entity. Should be a URI (reference to a vocabulary entry), UUID. E.g. 123, 123-321';


--
-- TOC entry 4851 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN platform.fk_identifier_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform.fk_identifier_codespace_id IS 'The codespace of the platform identifier, reference to the codespace table.';


--
-- TOC entry 4852 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN platform.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform.name IS 'The human readable name of the platform.';


--
-- TOC entry 4853 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN platform.fk_name_codespace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform.fk_name_codespace_id IS 'The codespace of the platform name, reference to the codespace table.';


--
-- TOC entry 4854 (class 0 OID 0)
-- Dependencies: 238
-- Name: COLUMN platform.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform.description IS 'A short description of the platform';


--
-- TOC entry 239 (class 1259 OID 20954)
-- Name: platform_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.platform_i18n (
    platform_i18n_id bigint NOT NULL,
    fk_platform_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE public.platform_i18n OWNER TO postgres;

--
-- TOC entry 4855 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN platform_i18n.platform_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_i18n.platform_i18n_id IS 'PK column of the table';


--
-- TOC entry 4856 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN platform_i18n.fk_platform_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_i18n.fk_platform_id IS 'Reference to the platform table this internationalization belongs to.';


--
-- TOC entry 4857 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN platform_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_i18n.locale IS 'Locale/language specification for this entry. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4858 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN platform_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_i18n.name IS 'Locale/language specific name of the platform';


--
-- TOC entry 4859 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN platform_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_i18n.description IS 'Locale/language specific description of the platform';


--
-- TOC entry 277 (class 1259 OID 21306)
-- Name: platform_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.platform_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.platform_i18n_seq OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 20962)
-- Name: platform_location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.platform_location (
    fk_location_id bigint NOT NULL,
    fk_platform_id bigint NOT NULL
);


ALTER TABLE public.platform_location OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 20967)
-- Name: platform_parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.platform_parameter (
    parameter_id bigint NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    last_update timestamp without time zone,
    domain character varying(255),
    fk_platform_id bigint NOT NULL,
    value_category character varying(255),
    fk_unit_id bigint,
    value_boolean smallint,
    value_count integer,
    value_quantity numeric(19,2),
    value_text character varying(255),
    value_xml text,
    value_json text,
    value_temporal_from timestamp without time zone,
    value_temporal_to timestamp without time zone,
    fk_parent_parameter_id bigint,
    CONSTRAINT platform_parameter_type_check CHECK (((type)::text = ANY ((ARRAY['bool'::character varying, 'category'::character varying, 'count'::character varying, 'quantity'::character varying, 'text'::character varying, 'xml'::character varying, 'json'::character varying, 'complex'::character varying, 'temporal'::character varying])::text[])))
);


ALTER TABLE public.platform_parameter OWNER TO postgres;

--
-- TOC entry 4860 (class 0 OID 0)
-- Dependencies: 241
-- Name: TABLE platform_parameter; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.platform_parameter IS 'Storage for additional information for platforms';


--
-- TOC entry 4861 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.parameter_id IS 'PK column of the table';


--
-- TOC entry 4862 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.type IS 'Indicator used by Hibernate to map value specific entities.';


--
-- TOC entry 4863 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.name IS 'The name of the parameter';


--
-- TOC entry 4864 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.description IS 'A short description of the parameter';


--
-- TOC entry 4865 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.last_update; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.last_update IS 'Timestamp that provides the time of the last modification of this entry';


--
-- TOC entry 4866 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.domain; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.domain IS 'The domain this parameter belongs to.';


--
-- TOC entry 4867 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.fk_platform_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.fk_platform_id IS 'Reference to the Platform this Parameter describes.';


--
-- TOC entry 4868 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_category IS 'Storage of a categorical parameter value.';


--
-- TOC entry 4869 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.fk_unit_id IS 'Reference to the unit of this value in the unit table';


--
-- TOC entry 4870 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_boolean; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_boolean IS 'Storage of a boolean parameter value.';


--
-- TOC entry 4871 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_count IS 'Storage of a count parameter value.';


--
-- TOC entry 4872 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_quantity IS 'Storage of a quantity parameter value.';


--
-- TOC entry 4873 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_text IS 'Storage of a textual parameter value.';


--
-- TOC entry 4874 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_xml IS 'Storage of a XML encoded parameter value.';


--
-- TOC entry 4875 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_json IS 'Storage of a JSON encoded parameter value.';


--
-- TOC entry 4876 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_temporal_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_temporal_from IS 'Storage of a temporal from parameter value.';


--
-- TOC entry 4877 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.value_temporal_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.value_temporal_to IS 'Storage of a temporal to parameter value.';


--
-- TOC entry 4878 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN platform_parameter.fk_parent_parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.platform_parameter.fk_parent_parameter_id IS 'Reference to the parent parameter';


--
-- TOC entry 278 (class 1259 OID 21308)
-- Name: platform_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.platform_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.platform_seq OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 20976)
-- Name: procedure; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.procedure (
    procedure_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    sta_identifier character varying(255) NOT NULL,
    fk_identifier_codespace_id bigint,
    name character varying(255),
    fk_name_codespace_id bigint,
    description text,
    description_file text,
    is_reference smallint DEFAULT 0,
    fk_type_of_procedure_id bigint,
    is_aggregation smallint DEFAULT 1,
    fk_format_id bigint NOT NULL,
    CONSTRAINT procedure_is_aggregation_check CHECK ((is_aggregation = ANY (ARRAY[1, 0]))),
    CONSTRAINT procedure_is_reference_check CHECK ((is_reference = ANY (ARRAY[1, 0])))
);


ALTER TABLE public.procedure OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 20988)
-- Name: procedure_hierarchy; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.procedure_hierarchy (
    fk_child_procedure_id bigint NOT NULL,
    fk_parent_procedure_id bigint NOT NULL
);


ALTER TABLE public.procedure_hierarchy OWNER TO postgres;

--
-- TOC entry 244 (class 1259 OID 20993)
-- Name: procedure_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.procedure_history (
    procedure_history_id bigint NOT NULL,
    fk_procedure_id bigint NOT NULL,
    fk_format_id bigint NOT NULL,
    valid_from timestamp without time zone NOT NULL,
    valid_to timestamp without time zone,
    xml text NOT NULL
);


ALTER TABLE public.procedure_history OWNER TO postgres;

--
-- TOC entry 4879 (class 0 OID 0)
-- Dependencies: 244
-- Name: TABLE procedure_history; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.procedure_history IS 'Storage of historical procedure descriptions as XML encoded text with period of validity.';


--
-- TOC entry 4880 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN procedure_history.procedure_history_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_history.procedure_history_id IS 'PK column of the table';


--
-- TOC entry 4881 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN procedure_history.fk_procedure_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_history.fk_procedure_id IS 'Reference to the procedure this entry belongs to.';


--
-- TOC entry 4882 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN procedure_history.fk_format_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_history.fk_format_id IS 'Reference to the format of the procedure description, e.g. SensorML 2.0';


--
-- TOC entry 4883 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN procedure_history.valid_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_history.valid_from IS 'The timestamp from which this procedure description is valid.';


--
-- TOC entry 4884 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN procedure_history.valid_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_history.valid_to IS 'The timestamp until this procedure description is valid. If null, this procedure description is currently valid';


--
-- TOC entry 4885 (class 0 OID 0)
-- Dependencies: 244
-- Name: COLUMN procedure_history.xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_history.xml IS 'XML representation of this procedure description';


--
-- TOC entry 279 (class 1259 OID 21310)
-- Name: procedure_history_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.procedure_history_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.procedure_history_seq OWNER TO postgres;

--
-- TOC entry 245 (class 1259 OID 21001)
-- Name: procedure_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.procedure_i18n (
    procedure_i18n_id bigint NOT NULL,
    fk_procedure_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text,
    short_name character varying(255),
    long_name character varying(255)
);


ALTER TABLE public.procedure_i18n OWNER TO postgres;

--
-- TOC entry 280 (class 1259 OID 21312)
-- Name: procedure_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.procedure_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.procedure_i18n_seq OWNER TO postgres;

--
-- TOC entry 246 (class 1259 OID 21009)
-- Name: procedure_parameter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.procedure_parameter (
    parameter_id bigint NOT NULL,
    type character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    last_update timestamp without time zone,
    domain character varying(255),
    fk_procedure_id bigint NOT NULL,
    value_boolean smallint,
    value_category character varying(255),
    fk_unit_id bigint,
    value_count integer,
    value_quantity numeric(19,2),
    value_text character varying(255),
    value_xml text,
    value_json text,
    value_temporal_from timestamp without time zone,
    value_temporal_to timestamp without time zone,
    fk_parent_parameter_id bigint,
    CONSTRAINT procedure_parameter_type_check CHECK (((type)::text = ANY ((ARRAY['bool'::character varying, 'category'::character varying, 'count'::character varying, 'quantity'::character varying, 'text'::character varying, 'xml'::character varying, 'json'::character varying, 'complex'::character varying, 'temporal'::character varying])::text[])))
);


ALTER TABLE public.procedure_parameter OWNER TO postgres;

--
-- TOC entry 4886 (class 0 OID 0)
-- Dependencies: 246
-- Name: TABLE procedure_parameter; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.procedure_parameter IS 'Storage for additional information for procedures';


--
-- TOC entry 4887 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.parameter_id IS 'PK column of the table';


--
-- TOC entry 4888 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.type IS 'Indicator used by Hibernate to map value specific entities.';


--
-- TOC entry 4889 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.name IS 'The name of the parameter';


--
-- TOC entry 4890 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.description IS 'A short description of the parameter';


--
-- TOC entry 4891 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.last_update; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.last_update IS 'Timestamp that provides the time of the last modification of this entry';


--
-- TOC entry 4892 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.domain; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.domain IS 'The domain this parameter belongs to.';


--
-- TOC entry 4893 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.fk_procedure_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.fk_procedure_id IS 'Reference to the Procedure this Parameter describes.';


--
-- TOC entry 4894 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_boolean; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_boolean IS 'Storage of a boolean parameter value.';


--
-- TOC entry 4895 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_category IS 'Storage of a categorical parameter value.';


--
-- TOC entry 4896 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.fk_unit_id IS 'Reference to the unit of this value in the unit table';


--
-- TOC entry 4897 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_count; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_count IS 'Storage of a count parameter value.';


--
-- TOC entry 4898 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_quantity IS 'Storage of a quantity parameter value.';


--
-- TOC entry 4899 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_text IS 'Storage of a textual parameter value.';


--
-- TOC entry 4900 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_xml; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_xml IS 'Storage of a XML encoded parameter value.';


--
-- TOC entry 4901 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_json IS 'Storage of a JSON encoded parameter value.';


--
-- TOC entry 4902 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_temporal_from; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_temporal_from IS 'Storage of a temporal from parameter value.';


--
-- TOC entry 4903 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.value_temporal_to; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.value_temporal_to IS 'Storage of a temporal to parameter value.';


--
-- TOC entry 4904 (class 0 OID 0)
-- Dependencies: 246
-- Name: COLUMN procedure_parameter.fk_parent_parameter_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.procedure_parameter.fk_parent_parameter_id IS 'Reference to the parent parameter';


--
-- TOC entry 281 (class 1259 OID 21314)
-- Name: procedure_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.procedure_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.procedure_seq OWNER TO postgres;

--
-- TOC entry 247 (class 1259 OID 21018)
-- Name: related_dataset; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.related_dataset (
    fk_dataset_id bigint NOT NULL,
    fk_related_dataset_id bigint NOT NULL,
    role character varying(255),
    url character varying(255)
);


ALTER TABLE public.related_dataset OWNER TO postgres;

--
-- TOC entry 4905 (class 0 OID 0)
-- Dependencies: 247
-- Name: TABLE related_dataset; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.related_dataset IS 'Store the relation of two datasets, e.g. one dataset depends on other datasets to provide context';


--
-- TOC entry 4906 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN related_dataset.fk_dataset_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_dataset.fk_dataset_id IS 'The reference to the dataset that has a related dataset';


--
-- TOC entry 4907 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN related_dataset.fk_related_dataset_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_dataset.fk_related_dataset_id IS 'The reference to the related dataset';


--
-- TOC entry 4908 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN related_dataset.role; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_dataset.role IS 'Definition of the role of the relation';


--
-- TOC entry 4909 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN related_dataset.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_dataset.url IS 'URL that point to external information';


--
-- TOC entry 248 (class 1259 OID 21026)
-- Name: related_feature; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.related_feature (
    related_feature_id bigint NOT NULL,
    fk_feature_id bigint NOT NULL,
    role character varying(255) NOT NULL
);


ALTER TABLE public.related_feature OWNER TO postgres;

--
-- TOC entry 4910 (class 0 OID 0)
-- Dependencies: 248
-- Name: TABLE related_feature; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.related_feature IS 'Storage of relations between offerings and features. This table is used by the SOS to fulfill the standard.';


--
-- TOC entry 4911 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN related_feature.related_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_feature.related_feature_id IS 'PK column of the table';


--
-- TOC entry 4912 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN related_feature.fk_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_feature.fk_feature_id IS 'Reference to the feature that is related to the offering.';


--
-- TOC entry 4913 (class 0 OID 0)
-- Dependencies: 248
-- Name: COLUMN related_feature.role; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_feature.role IS 'The role of the related feature.';


--
-- TOC entry 282 (class 1259 OID 21316)
-- Name: related_feature_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.related_feature_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.related_feature_seq OWNER TO postgres;

--
-- TOC entry 249 (class 1259 OID 21031)
-- Name: related_observation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.related_observation (
    fk_observation_id bigint NOT NULL,
    fk_related_observation_id bigint NOT NULL,
    role character varying(255),
    url character varying(255)
);


ALTER TABLE public.related_observation OWNER TO postgres;

--
-- TOC entry 4914 (class 0 OID 0)
-- Dependencies: 249
-- Name: TABLE related_observation; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.related_observation IS 'Store the relation of two observation, e.g. one observation depends on other observations to provide context';


--
-- TOC entry 4915 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN related_observation.fk_observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_observation.fk_observation_id IS 'The reference to the dataset that has a related data/observation';


--
-- TOC entry 4916 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN related_observation.fk_related_observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_observation.fk_related_observation_id IS 'The reference to the related data/observation';


--
-- TOC entry 4917 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN related_observation.role; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_observation.role IS 'Definition of the role of the relation';


--
-- TOC entry 4918 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN related_observation.url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.related_observation.url IS 'URL that point to external information';


--
-- TOC entry 250 (class 1259 OID 21039)
-- Name: result_template; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.result_template (
    result_template_id bigint NOT NULL,
    fk_offering_id bigint NOT NULL,
    fk_phenomenon_id bigint NOT NULL,
    fk_procedure_id bigint,
    fk_feature_id bigint,
    fk_category_id bigint NOT NULL,
    identifier character varying(255) NOT NULL,
    structure text,
    encoding text,
    observation_structure text,
    observation_encoding text
);


ALTER TABLE public.result_template OWNER TO postgres;

--
-- TOC entry 4919 (class 0 OID 0)
-- Dependencies: 250
-- Name: TABLE result_template; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.result_template IS 'Storage of templates for the result handling operations';


--
-- TOC entry 4920 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.result_template_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.result_template_id IS 'PK column of the table';


--
-- TOC entry 4921 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.fk_offering_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.fk_offering_id IS 'The offering that is associated with the result template';


--
-- TOC entry 4922 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.fk_phenomenon_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.fk_phenomenon_id IS 'The phenomenon that is associated with the result template';


--
-- TOC entry 4923 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.fk_procedure_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.fk_procedure_id IS 'The procedure that is associated with the result template. Can be null if the feature is defined in the structure.';


--
-- TOC entry 4924 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.fk_feature_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.fk_feature_id IS 'The feature that is associated with the result template. Can be null if the feature is defined in the structure.';


--
-- TOC entry 4925 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.fk_category_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.fk_category_id IS 'The category that is associated with the result template';


--
-- TOC entry 4926 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.identifier IS 'Unique identifier of the result template used for insertion operation';


--
-- TOC entry 4927 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.structure; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.structure IS 'The structure of the result template, should be a XML encoded swe:DataRecord';


--
-- TOC entry 4928 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.encoding; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.encoding IS 'The encding of the result template, should be a XML encoded swe:TextEncoding';


--
-- TOC entry 4929 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.observation_structure; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.observation_structure IS 'The structure of the result template used for observations, should be a XML encoded swe:DataRecord';


--
-- TOC entry 4930 (class 0 OID 0)
-- Dependencies: 250
-- Name: COLUMN result_template.observation_encoding; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.result_template.observation_encoding IS 'The encding of the result template used for observations, should be a XML encoded swe:TextEncoding';


--
-- TOC entry 283 (class 1259 OID 21318)
-- Name: result_template_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.result_template_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.result_template_seq OWNER TO postgres;

--
-- TOC entry 251 (class 1259 OID 21047)
-- Name: tag; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tag (
    tag_id bigint NOT NULL,
    identifier character varying(255),
    description text
);


ALTER TABLE public.tag OWNER TO postgres;

--
-- TOC entry 4931 (class 0 OID 0)
-- Dependencies: 251
-- Name: TABLE tag; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tag IS 'Storage of the tags which should be used to tag the data.';


--
-- TOC entry 4932 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN tag.tag_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag.tag_id IS 'PK column of the table';


--
-- TOC entry 4933 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN tag.identifier; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag.identifier IS 'Unique identifier/name of the tag which can be used for filtering.';


--
-- TOC entry 4934 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN tag.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag.description IS 'A short description of the tag';


--
-- TOC entry 252 (class 1259 OID 21055)
-- Name: tag_dataset; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tag_dataset (
    fk_dataset_id bigint NOT NULL,
    fk_tag_id bigint NOT NULL
);


ALTER TABLE public.tag_dataset OWNER TO postgres;

--
-- TOC entry 4935 (class 0 OID 0)
-- Dependencies: 252
-- Name: TABLE tag_dataset; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tag_dataset IS 'Storage of relations between dataset and related tags';


--
-- TOC entry 4936 (class 0 OID 0)
-- Dependencies: 252
-- Name: COLUMN tag_dataset.fk_dataset_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag_dataset.fk_dataset_id IS 'The reference to the dataset in the dataset table';


--
-- TOC entry 4937 (class 0 OID 0)
-- Dependencies: 252
-- Name: COLUMN tag_dataset.fk_tag_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag_dataset.fk_tag_id IS 'The reference to the tags in the tag dataset table';


--
-- TOC entry 253 (class 1259 OID 21060)
-- Name: tag_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tag_i18n (
    tag_i18n_id bigint NOT NULL,
    fk_tag_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255),
    description text
);


ALTER TABLE public.tag_i18n OWNER TO postgres;

--
-- TOC entry 4938 (class 0 OID 0)
-- Dependencies: 253
-- Name: TABLE tag_i18n; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tag_i18n IS 'Storage for internationalizations of tags.';


--
-- TOC entry 4939 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN tag_i18n.tag_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag_i18n.tag_i18n_id IS 'PK column of the table';


--
-- TOC entry 4940 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN tag_i18n.fk_tag_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag_i18n.fk_tag_id IS 'Reference to the tag table this internationalization belongs to. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4941 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN tag_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag_i18n.locale IS 'Locale/language specification for this entry';


--
-- TOC entry 4942 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN tag_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag_i18n.name IS 'Locale/language specific name of the tag';


--
-- TOC entry 4943 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN tag_i18n.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tag_i18n.description IS 'Locale/language specific description of the tag';


--
-- TOC entry 284 (class 1259 OID 21320)
-- Name: tag_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tag_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tag_i18n_seq OWNER TO postgres;

--
-- TOC entry 285 (class 1259 OID 21322)
-- Name: tag_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tag_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tag_seq OWNER TO postgres;

--
-- TOC entry 254 (class 1259 OID 21068)
-- Name: unit; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.unit (
    unit_id bigint NOT NULL,
    symbol character varying(255) NOT NULL,
    name character varying(255),
    link character varying(255)
);


ALTER TABLE public.unit OWNER TO postgres;

--
-- TOC entry 4944 (class 0 OID 0)
-- Dependencies: 254
-- Name: TABLE unit; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.unit IS 'Storage of the units of measurement of the observation values. These may be °C or m as the unit for depth/height information.';


--
-- TOC entry 4945 (class 0 OID 0)
-- Dependencies: 254
-- Name: COLUMN unit.unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.unit.unit_id IS 'PK column of the table';


--
-- TOC entry 4946 (class 0 OID 0)
-- Dependencies: 254
-- Name: COLUMN unit.symbol; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.unit.symbol IS 'The symbol of the unit, e.g. °C or m.';


--
-- TOC entry 4947 (class 0 OID 0)
-- Dependencies: 254
-- Name: COLUMN unit.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.unit.name IS 'Human readable name of the unit, e.g degree celsius or meter';


--
-- TOC entry 4948 (class 0 OID 0)
-- Dependencies: 254
-- Name: COLUMN unit.link; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.unit.link IS 'Link/reference to an external description of the unit, e.g. to a vocabulary..';


--
-- TOC entry 255 (class 1259 OID 21076)
-- Name: unit_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.unit_i18n (
    unit_i18n_id bigint NOT NULL,
    fk_unit_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    name character varying(255)
);


ALTER TABLE public.unit_i18n OWNER TO postgres;

--
-- TOC entry 4949 (class 0 OID 0)
-- Dependencies: 255
-- Name: TABLE unit_i18n; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.unit_i18n IS 'Storage for internationalizations of units.';


--
-- TOC entry 4950 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN unit_i18n.unit_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.unit_i18n.unit_i18n_id IS 'PK column of the table';


--
-- TOC entry 4951 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN unit_i18n.fk_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.unit_i18n.fk_unit_id IS 'Reference to the unit table this internationalization belongs to.';


--
-- TOC entry 4952 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN unit_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.unit_i18n.locale IS 'Locale/language specification for this unit. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4953 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN unit_i18n.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.unit_i18n.name IS 'Locale/language specific name of the unit';


--
-- TOC entry 286 (class 1259 OID 21324)
-- Name: unit_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.unit_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.unit_i18n_seq OWNER TO postgres;

--
-- TOC entry 287 (class 1259 OID 21326)
-- Name: unit_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.unit_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.unit_seq OWNER TO postgres;

--
-- TOC entry 256 (class 1259 OID 21084)
-- Name: value_blob; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.value_blob (
    fk_observation_id bigint NOT NULL,
    value_blob oid
);


ALTER TABLE public.value_blob OWNER TO postgres;

--
-- TOC entry 4954 (class 0 OID 0)
-- Dependencies: 256
-- Name: COLUMN value_blob.fk_observation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_blob.fk_observation_id IS 'Reference to the data/observation in the observation table';


--
-- TOC entry 4955 (class 0 OID 0)
-- Dependencies: 256
-- Name: COLUMN value_blob.value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_blob.value_blob IS 'The blob value of an observation';


--
-- TOC entry 257 (class 1259 OID 21089)
-- Name: value_profile; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.value_profile (
    value_profile_id bigint NOT NULL,
    orientation smallint,
    vertical_origin_name character varying(255),
    vertical_from_name character varying(255),
    vertical_to_name character varying(255),
    fk_vertical_unit_id bigint NOT NULL
);


ALTER TABLE public.value_profile OWNER TO postgres;

--
-- TOC entry 4956 (class 0 OID 0)
-- Dependencies: 257
-- Name: TABLE value_profile; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.value_profile IS 'Storage of meta-information about a profile measurement. These are the orientation (height/depth) and name of the depth/height value as they should be named in the output and the unit of the depth/height value. A value_profile must be defined for each dataset containing profile data.';


--
-- TOC entry 4957 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN value_profile.value_profile_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile.value_profile_id IS 'PK column of the table';


--
-- TOC entry 4958 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN value_profile.orientation; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile.orientation IS 'The "orientation" of the vertical values as integer. 1 => above verticalOriginName and -1 => below verticalOriginName';


--
-- TOC entry 4959 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN value_profile.vertical_origin_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile.vertical_origin_name IS 'The vertical origin name of the vertical values, e.g. water surface';


--
-- TOC entry 4960 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN value_profile.vertical_from_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile.vertical_from_name IS 'The name of the vertical from values, e.g. from or depthFrom';


--
-- TOC entry 4961 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN value_profile.vertical_to_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile.vertical_to_name IS 'The name of the vertical from values, e.g. to or depthTo';


--
-- TOC entry 4962 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN value_profile.fk_vertical_unit_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile.fk_vertical_unit_id IS 'The unit of the vertical value, e.g. m';


--
-- TOC entry 258 (class 1259 OID 21097)
-- Name: value_profile_i18n; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.value_profile_i18n (
    value_profile_i18n_id bigint NOT NULL,
    fk_value_profile_id bigint NOT NULL,
    locale character varying(255) NOT NULL,
    vertical_origin_name character varying(255),
    vertical_from_name character varying(255),
    vertical_to_name character varying(255)
);


ALTER TABLE public.value_profile_i18n OWNER TO postgres;

--
-- TOC entry 4963 (class 0 OID 0)
-- Dependencies: 258
-- Name: COLUMN value_profile_i18n.value_profile_i18n_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile_i18n.value_profile_i18n_id IS 'PK column of the table';


--
-- TOC entry 4964 (class 0 OID 0)
-- Dependencies: 258
-- Name: COLUMN value_profile_i18n.fk_value_profile_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile_i18n.fk_value_profile_id IS 'Reference to the value_profile table this internationalization belongs to.';


--
-- TOC entry 4965 (class 0 OID 0)
-- Dependencies: 258
-- Name: COLUMN value_profile_i18n.locale; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile_i18n.locale IS 'Locale/language specification for this entry. ISO 639 Codes (http://www.loc.gov/standards/iso639-2/php/code_list.php)';


--
-- TOC entry 4966 (class 0 OID 0)
-- Dependencies: 258
-- Name: COLUMN value_profile_i18n.vertical_origin_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile_i18n.vertical_origin_name IS 'Locale/language specific vertical origin name of the vertical metadata entity';


--
-- TOC entry 4967 (class 0 OID 0)
-- Dependencies: 258
-- Name: COLUMN value_profile_i18n.vertical_from_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile_i18n.vertical_from_name IS 'Locale/language specific verticalTo name of the vertical metadata entity';


--
-- TOC entry 4968 (class 0 OID 0)
-- Dependencies: 258
-- Name: COLUMN value_profile_i18n.vertical_to_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.value_profile_i18n.vertical_to_name IS 'Locale/language specific verticalTo name of the vertical metadata entity';


--
-- TOC entry 288 (class 1259 OID 21328)
-- Name: value_profile_i18n_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.value_profile_i18n_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.value_profile_i18n_seq OWNER TO postgres;

--
-- TOC entry 289 (class 1259 OID 21330)
-- Name: value_profile_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.value_profile_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4969 (class 0 OID 0)
-- Dependencies: 259
-- Name: category_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.category_i18n_seq', 1, false);


--
-- TOC entry 4970 (class 0 OID 0)
-- Dependencies: 260
-- Name: category_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.category_seq', 1, true);


--
-- TOC entry 4971 (class 0 OID 0)
-- Dependencies: 261
-- Name: codespace_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.codespace_seq', 1, false);


--
-- TOC entry 4972 (class 0 OID 0)
-- Dependencies: 262
-- Name: dataset_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.dataset_i18n_seq', 1, false);


--
-- TOC entry 4973 (class 0 OID 0)
-- Dependencies: 263
-- Name: dataset_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.dataset_seq', 2, true);


--
-- TOC entry 4974 (class 0 OID 0)
-- Dependencies: 264
-- Name: feature_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.feature_i18n_seq', 1, false);


--
-- TOC entry 4975 (class 0 OID 0)
-- Dependencies: 265
-- Name: feature_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.feature_seq', 1, true);


--
-- TOC entry 4976 (class 0 OID 0)
-- Dependencies: 266
-- Name: format_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.format_seq', 14, true);


--
-- TOC entry 4977 (class 0 OID 0)
-- Dependencies: 267
-- Name: historical_location_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.historical_location_seq', 1, true);


--
-- TOC entry 4978 (class 0 OID 0)
-- Dependencies: 268
-- Name: location_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.location_i18n_seq', 1, false);


--
-- TOC entry 4979 (class 0 OID 0)
-- Dependencies: 269
-- Name: location_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.location_seq', 1, true);


--
-- TOC entry 4980 (class 0 OID 0)
-- Dependencies: 270
-- Name: observation_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.observation_i18n_seq', 1, false);


--
-- TOC entry 4981 (class 0 OID 0)
-- Dependencies: 271
-- Name: observation_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.observation_seq', 4, true);


--
-- TOC entry 4982 (class 0 OID 0)
-- Dependencies: 272
-- Name: offering_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.offering_i18n_seq', 1, false);


--
-- TOC entry 4983 (class 0 OID 0)
-- Dependencies: 273
-- Name: offering_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.offering_seq', 2, true);


--
-- TOC entry 4984 (class 0 OID 0)
-- Dependencies: 274
-- Name: parameter_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.parameter_seq', 1, true);


--
-- TOC entry 4985 (class 0 OID 0)
-- Dependencies: 275
-- Name: phenomenon_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.phenomenon_i18n_seq', 1, false);


--
-- TOC entry 4986 (class 0 OID 0)
-- Dependencies: 276
-- Name: phenomenon_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.phenomenon_seq', 2, true);


--
-- TOC entry 4987 (class 0 OID 0)
-- Dependencies: 277
-- Name: platform_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.platform_i18n_seq', 1, false);


--
-- TOC entry 4988 (class 0 OID 0)
-- Dependencies: 278
-- Name: platform_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.platform_seq', 1, true);


--
-- TOC entry 4989 (class 0 OID 0)
-- Dependencies: 279
-- Name: procedure_history_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.procedure_history_seq', 1, false);


--
-- TOC entry 4990 (class 0 OID 0)
-- Dependencies: 280
-- Name: procedure_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.procedure_i18n_seq', 1, false);


--
-- TOC entry 4991 (class 0 OID 0)
-- Dependencies: 281
-- Name: procedure_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.procedure_seq', 2, true);


--
-- TOC entry 4992 (class 0 OID 0)
-- Dependencies: 282
-- Name: related_feature_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.related_feature_seq', 1, false);


--
-- TOC entry 4993 (class 0 OID 0)
-- Dependencies: 283
-- Name: result_template_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.result_template_seq', 1, false);


--
-- TOC entry 4994 (class 0 OID 0)
-- Dependencies: 284
-- Name: tag_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.tag_i18n_seq', 1, false);


--
-- TOC entry 4995 (class 0 OID 0)
-- Dependencies: 285
-- Name: tag_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.tag_seq', 1, false);


--
-- TOC entry 4996 (class 0 OID 0)
-- Dependencies: 286
-- Name: unit_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.unit_i18n_seq', 1, false);


--
-- TOC entry 4997 (class 0 OID 0)
-- Dependencies: 287
-- Name: unit_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.unit_seq', 2, true);


--
-- TOC entry 4998 (class 0 OID 0)
-- Dependencies: 288
-- Name: value_profile_i18n_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.value_profile_i18n_seq', 1, false);


--
-- TOC entry 4999 (class 0 OID 0)
-- Dependencies: 289
-- Name: value_profile_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.value_profile_seq', 1, false);


ALTER TABLE public.value_profile_seq OWNER TO postgres;


--
-- TOC entry 3989 (class 2606 OID 20722)
-- Name: category_i18n category_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_i18n
    ADD CONSTRAINT category_i18n_pkey PRIMARY KEY (category_i18n_id);


--
-- TOC entry 3984 (class 2606 OID 20714)
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (category_id);


--
-- TOC entry 3992 (class 2606 OID 20727)
-- Name: codespace codespace_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.codespace
    ADD CONSTRAINT codespace_pkey PRIMARY KEY (codespace_id);


--
-- TOC entry 3996 (class 2606 OID 20732)
-- Name: composite_phenomenon composite_phenomenon_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.composite_phenomenon
    ADD CONSTRAINT composite_phenomenon_pkey PRIMARY KEY (fk_parent_phenomenon_id, fk_child_phenomenon_id);


--
-- TOC entry 4025 (class 2606 OID 20766)
-- Name: dataset_i18n dataset_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_i18n
    ADD CONSTRAINT dataset_i18n_pkey PRIMARY KEY (dataset_i18n_id);


--
-- TOC entry 4028 (class 2606 OID 20775)
-- Name: dataset_parameter dataset_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_parameter
    ADD CONSTRAINT dataset_parameter_pkey PRIMARY KEY (parameter_id);


--
-- TOC entry 3999 (class 2606 OID 20758)
-- Name: dataset dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT dataset_pkey PRIMARY KEY (dataset_id);


--
-- TOC entry 4033 (class 2606 OID 20780)
-- Name: dataset_reference dataset_reference_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_reference
    ADD CONSTRAINT dataset_reference_pkey PRIMARY KEY (fk_dataset_id_from, sort_order);


--
-- TOC entry 4049 (class 2606 OID 20793)
-- Name: feature_hierarchy feature_hierarchy_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_hierarchy
    ADD CONSTRAINT feature_hierarchy_pkey PRIMARY KEY (fk_parent_feature_id, fk_child_feature_id);


--
-- TOC entry 4052 (class 2606 OID 20801)
-- Name: feature_i18n feature_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_i18n
    ADD CONSTRAINT feature_i18n_pkey PRIMARY KEY (feature_i18n_id);


--
-- TOC entry 4055 (class 2606 OID 20810)
-- Name: feature_parameter feature_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_parameter
    ADD CONSTRAINT feature_parameter_pkey PRIMARY KEY (parameter_id);


--
-- TOC entry 4036 (class 2606 OID 20788)
-- Name: feature feature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature
    ADD CONSTRAINT feature_pkey PRIMARY KEY (feature_id);


--
-- TOC entry 4060 (class 2606 OID 20815)
-- Name: format format_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.format
    ADD CONSTRAINT format_pkey PRIMARY KEY (format_id);


--
-- TOC entry 4064 (class 2606 OID 20823)
-- Name: historical_location historical_location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historical_location
    ADD CONSTRAINT historical_location_pkey PRIMARY KEY (historical_location_id);


--
-- TOC entry 4083 (class 2606 OID 20836)
-- Name: location_historical_location location_historical_location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_historical_location
    ADD CONSTRAINT location_historical_location_pkey PRIMARY KEY (fk_location_id, fk_historical_location_id);


--
-- TOC entry 4086 (class 2606 OID 20844)
-- Name: location_i18n location_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_i18n
    ADD CONSTRAINT location_i18n_pkey PRIMARY KEY (location_i18n_id);


--
-- TOC entry 4091 (class 2606 OID 20853)
-- Name: location_parameter location_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_parameter
    ADD CONSTRAINT location_parameter_pkey PRIMARY KEY (parameter_id);


--
-- TOC entry 4076 (class 2606 OID 20831)
-- Name: location location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location
    ADD CONSTRAINT location_pkey PRIMARY KEY (location_id);


--
-- TOC entry 4112 (class 2606 OID 20875)
-- Name: observation_i18n observation_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation_i18n
    ADD CONSTRAINT observation_i18n_pkey PRIMARY KEY (observation_i18n_id);


--
-- TOC entry 4117 (class 2606 OID 20884)
-- Name: observation_parameter observation_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation_parameter
    ADD CONSTRAINT observation_parameter_pkey PRIMARY KEY (parameter_id);


--
-- TOC entry 4103 (class 2606 OID 20867)
-- Name: observation observation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT observation_pkey PRIMARY KEY (observation_id);


--
-- TOC entry 4127 (class 2606 OID 20897)
-- Name: offering_feature_type offering_feature_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_feature_type
    ADD CONSTRAINT offering_feature_type_pkey PRIMARY KEY (fk_offering_id, fk_format_id);


--
-- TOC entry 4130 (class 2606 OID 20902)
-- Name: offering_hierarchy offering_hierarchy_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_hierarchy
    ADD CONSTRAINT offering_hierarchy_pkey PRIMARY KEY (fk_parent_offering_id, fk_child_offering_id);


--
-- TOC entry 4133 (class 2606 OID 20910)
-- Name: offering_i18n offering_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_i18n
    ADD CONSTRAINT offering_i18n_pkey PRIMARY KEY (offering_i18n_id);


--
-- TOC entry 4136 (class 2606 OID 20915)
-- Name: offering_observation_type offering_observation_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_observation_type
    ADD CONSTRAINT offering_observation_type_pkey PRIMARY KEY (fk_offering_id, fk_format_id);


--
-- TOC entry 4122 (class 2606 OID 20892)
-- Name: offering offering_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering
    ADD CONSTRAINT offering_pkey PRIMARY KEY (offering_id);


--
-- TOC entry 4139 (class 2606 OID 20920)
-- Name: offering_related_feature offering_related_feature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_related_feature
    ADD CONSTRAINT offering_related_feature_pkey PRIMARY KEY (fk_offering_id, fk_related_feature_id);


--
-- TOC entry 4152 (class 2606 OID 20936)
-- Name: phenomenon_i18n phenomenon_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon_i18n
    ADD CONSTRAINT phenomenon_i18n_pkey PRIMARY KEY (phenomenon_i18n_id);


--
-- TOC entry 4157 (class 2606 OID 20945)
-- Name: phenomenon_parameter phenomenon_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon_parameter
    ADD CONSTRAINT phenomenon_parameter_pkey PRIMARY KEY (parameter_id);


--
-- TOC entry 4145 (class 2606 OID 20928)
-- Name: phenomenon phenomenon_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT phenomenon_pkey PRIMARY KEY (phenomenon_id);


--
-- TOC entry 4170 (class 2606 OID 20961)
-- Name: platform_i18n platform_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_i18n
    ADD CONSTRAINT platform_i18n_pkey PRIMARY KEY (platform_i18n_id);


--
-- TOC entry 4174 (class 2606 OID 20966)
-- Name: platform_location platform_location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_location
    ADD CONSTRAINT platform_location_pkey PRIMARY KEY (fk_platform_id, fk_location_id);


--
-- TOC entry 4179 (class 2606 OID 20975)
-- Name: platform_parameter platform_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_parameter
    ADD CONSTRAINT platform_parameter_pkey PRIMARY KEY (parameter_id);


--
-- TOC entry 4163 (class 2606 OID 20953)
-- Name: platform platform_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform
    ADD CONSTRAINT platform_pkey PRIMARY KEY (platform_id);


--
-- TOC entry 4194 (class 2606 OID 20992)
-- Name: procedure_hierarchy procedure_hierarchy_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_hierarchy
    ADD CONSTRAINT procedure_hierarchy_pkey PRIMARY KEY (fk_parent_procedure_id, fk_child_procedure_id);


--
-- TOC entry 4200 (class 2606 OID 21000)
-- Name: procedure_history procedure_history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_history
    ADD CONSTRAINT procedure_history_pkey PRIMARY KEY (procedure_history_id);


--
-- TOC entry 4203 (class 2606 OID 21008)
-- Name: procedure_i18n procedure_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_i18n
    ADD CONSTRAINT procedure_i18n_pkey PRIMARY KEY (procedure_i18n_id);


--
-- TOC entry 4208 (class 2606 OID 21017)
-- Name: procedure_parameter procedure_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_parameter
    ADD CONSTRAINT procedure_parameter_pkey PRIMARY KEY (parameter_id);


--
-- TOC entry 4187 (class 2606 OID 20987)
-- Name: procedure procedure_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT procedure_pkey PRIMARY KEY (procedure_id);


--
-- TOC entry 4211 (class 2606 OID 21025)
-- Name: related_dataset related_dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.related_dataset
    ADD CONSTRAINT related_dataset_pkey PRIMARY KEY (fk_dataset_id, fk_related_dataset_id);


--
-- TOC entry 4214 (class 2606 OID 21030)
-- Name: related_feature related_feature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.related_feature
    ADD CONSTRAINT related_feature_pkey PRIMARY KEY (related_feature_id);


--
-- TOC entry 4218 (class 2606 OID 21038)
-- Name: related_observation related_observation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.related_observation
    ADD CONSTRAINT related_observation_pkey PRIMARY KEY (fk_observation_id, fk_related_observation_id);


--
-- TOC entry 4226 (class 2606 OID 21046)
-- Name: result_template result_template_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT result_template_pkey PRIMARY KEY (result_template_id);


--
-- TOC entry 4231 (class 2606 OID 21059)
-- Name: tag_dataset tag_dataset_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag_dataset
    ADD CONSTRAINT tag_dataset_pkey PRIMARY KEY (fk_tag_id, fk_dataset_id);


--
-- TOC entry 4234 (class 2606 OID 21067)
-- Name: tag_i18n tag_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag_i18n
    ADD CONSTRAINT tag_i18n_pkey PRIMARY KEY (tag_i18n_id);


--
-- TOC entry 4228 (class 2606 OID 21054)
-- Name: tag tag_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (tag_id);


--
-- TOC entry 3987 (class 2606 OID 21107)
-- Name: category un_category_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT un_category_identifier UNIQUE (identifier);


--
-- TOC entry 3994 (class 2606 OID 21110)
-- Name: codespace un_codespace_codespace; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.codespace
    ADD CONSTRAINT un_codespace_codespace UNIQUE (name);


--
-- TOC entry 4019 (class 2606 OID 21131)
-- Name: dataset un_dataset_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT un_dataset_identifier UNIQUE (identifier);


--
-- TOC entry 4021 (class 2606 OID 21135)
-- Name: dataset un_dataset_identity; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT un_dataset_identity UNIQUE (fk_procedure_id, fk_phenomenon_id, fk_offering_id, fk_category_id, fk_feature_id, fk_platform_id, fk_unit_id);


--
-- TOC entry 4023 (class 2606 OID 21133)
-- Name: dataset un_dataset_staidentifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT un_dataset_staidentifier UNIQUE (sta_identifier);


--
-- TOC entry 4043 (class 2606 OID 21147)
-- Name: feature un_feature_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature
    ADD CONSTRAINT un_feature_identifier UNIQUE (identifier);


--
-- TOC entry 4045 (class 2606 OID 21149)
-- Name: feature un_feature_staidentifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature
    ADD CONSTRAINT un_feature_staidentifier UNIQUE (sta_identifier);


--
-- TOC entry 4047 (class 2606 OID 21151)
-- Name: feature un_feature_url; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature
    ADD CONSTRAINT un_feature_url UNIQUE (url);


--
-- TOC entry 4062 (class 2606 OID 21158)
-- Name: format un_format_definition; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.format
    ADD CONSTRAINT un_format_definition UNIQUE (definition);


--
-- TOC entry 4069 (class 2606 OID 21163)
-- Name: historical_location un_historicallocation_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historical_location
    ADD CONSTRAINT un_historicallocation_identifier UNIQUE (identifier);


--
-- TOC entry 4071 (class 2606 OID 21165)
-- Name: historical_location un_historicallocation_staidentifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historical_location
    ADD CONSTRAINT un_historicallocation_staidentifier UNIQUE (sta_identifier);


--
-- TOC entry 4078 (class 2606 OID 21170)
-- Name: location un_location_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location
    ADD CONSTRAINT un_location_identifier UNIQUE (identifier);


--
-- TOC entry 4080 (class 2606 OID 21172)
-- Name: location un_location_staidentifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location
    ADD CONSTRAINT un_location_staidentifier UNIQUE (sta_identifier);


--
-- TOC entry 4105 (class 2606 OID 21191)
-- Name: observation un_observation_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT un_observation_identifier UNIQUE (identifier);


--
-- TOC entry 4107 (class 2606 OID 21189)
-- Name: observation un_observation_identity; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT un_observation_identity UNIQUE (value_type, fk_dataset_id, sampling_time_start, sampling_time_end, result_time, vertical_from, vertical_to);


--
-- TOC entry 4109 (class 2606 OID 21193)
-- Name: observation un_observation_staidentifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT un_observation_staidentifier UNIQUE (sta_identifier);


--
-- TOC entry 4124 (class 2606 OID 21202)
-- Name: offering un_offering_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering
    ADD CONSTRAINT un_offering_identifier UNIQUE (identifier);


--
-- TOC entry 4147 (class 2606 OID 21213)
-- Name: phenomenon un_phenomenon_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT un_phenomenon_identifier UNIQUE (identifier);


--
-- TOC entry 4149 (class 2606 OID 21215)
-- Name: phenomenon un_phenomenon_staidentifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT un_phenomenon_staidentifier UNIQUE (sta_identifier);


--
-- TOC entry 4165 (class 2606 OID 21225)
-- Name: platform un_platform_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform
    ADD CONSTRAINT un_platform_identifier UNIQUE (identifier);


--
-- TOC entry 4167 (class 2606 OID 21227)
-- Name: platform un_platform_staidentifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform
    ADD CONSTRAINT un_platform_staidentifier UNIQUE (sta_identifier);


--
-- TOC entry 4189 (class 2606 OID 21241)
-- Name: procedure un_procedure_identifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT un_procedure_identifier UNIQUE (identifier);


--
-- TOC entry 4191 (class 2606 OID 21243)
-- Name: procedure un_procedure_staidentifier; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT un_procedure_staidentifier UNIQUE (sta_identifier);


--
-- TOC entry 4236 (class 2606 OID 21266)
-- Name: unit un_unit_symbol; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unit
    ADD CONSTRAINT un_unit_symbol UNIQUE (symbol);


--
-- TOC entry 4241 (class 2606 OID 21083)
-- Name: unit_i18n unit_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unit_i18n
    ADD CONSTRAINT unit_i18n_pkey PRIMARY KEY (unit_i18n_id);


--
-- TOC entry 4238 (class 2606 OID 21075)
-- Name: unit unit_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unit
    ADD CONSTRAINT unit_pkey PRIMARY KEY (unit_id);


--
-- TOC entry 4243 (class 2606 OID 21088)
-- Name: value_blob value_blob_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.value_blob
    ADD CONSTRAINT value_blob_pkey PRIMARY KEY (fk_observation_id);


--
-- TOC entry 4249 (class 2606 OID 21104)
-- Name: value_profile_i18n value_profile_i18n_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.value_profile_i18n
    ADD CONSTRAINT value_profile_i18n_pkey PRIMARY KEY (value_profile_i18n_id);


--
-- TOC entry 4246 (class 2606 OID 21096)
-- Name: value_profile value_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.value_profile
    ADD CONSTRAINT value_profile_pkey PRIMARY KEY (value_profile_id);


--
-- TOC entry 3990 (class 1259 OID 21108)
-- Name: idx_category_i18n_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_category_i18n_category ON public.category_i18n USING btree (fk_category_id);


--
-- TOC entry 3985 (class 1259 OID 21105)
-- Name: idx_category_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_category_identifier ON public.category USING btree (identifier);


--
-- TOC entry 4000 (class 1259 OID 21122)
-- Name: idx_dataset_aggregation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_aggregation ON public.dataset USING btree (fk_aggregation_id);


--
-- TOC entry 4001 (class 1259 OID 21117)
-- Name: idx_dataset_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_category ON public.dataset USING btree (fk_category_id);


--
-- TOC entry 4002 (class 1259 OID 21125)
-- Name: idx_dataset_dataset_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_dataset_type ON public.dataset USING btree (dataset_type);


--
-- TOC entry 4003 (class 1259 OID 21118)
-- Name: idx_dataset_feature; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_feature ON public.dataset USING btree (fk_feature_id);


--
-- TOC entry 4004 (class 1259 OID 21123)
-- Name: idx_dataset_first_observation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_first_observation ON public.dataset USING btree (fk_first_observation_id);


--
-- TOC entry 4026 (class 1259 OID 21136)
-- Name: idx_dataset_i18n_dataset; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_i18n_dataset ON public.dataset_i18n USING btree (fk_dataset_id);


--
-- TOC entry 4005 (class 1259 OID 21112)
-- Name: idx_dataset_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_identifier ON public.dataset USING btree (identifier);


--
-- TOC entry 4006 (class 1259 OID 21127)
-- Name: idx_dataset_identifier_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_identifier_codespace ON public.dataset USING btree (fk_identifier_codespace_id);


--
-- TOC entry 4007 (class 1259 OID 21124)
-- Name: idx_dataset_last_observation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_last_observation ON public.dataset USING btree (fk_last_observation_id);


--
-- TOC entry 4008 (class 1259 OID 21128)
-- Name: idx_dataset_name_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_name_codespace ON public.dataset USING btree (fk_name_codespace_id);


--
-- TOC entry 4009 (class 1259 OID 21121)
-- Name: idx_dataset_observation_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_observation_type ON public.dataset USING btree (fk_format_id, observation_type);


--
-- TOC entry 4010 (class 1259 OID 21116)
-- Name: idx_dataset_offering; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_offering ON public.dataset USING btree (fk_offering_id);


--
-- TOC entry 4029 (class 1259 OID 21137)
-- Name: idx_dataset_param_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_param_name ON public.dataset_parameter USING btree (name);


--
-- TOC entry 4030 (class 1259 OID 21138)
-- Name: idx_dataset_parameter; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_parameter ON public.dataset_parameter USING btree (fk_dataset_id);


--
-- TOC entry 4031 (class 1259 OID 21139)
-- Name: idx_dataset_parameter_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_parameter_unit ON public.dataset_parameter USING btree (fk_unit_id);


--
-- TOC entry 4011 (class 1259 OID 21115)
-- Name: idx_dataset_phenomenon; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_phenomenon ON public.dataset USING btree (fk_phenomenon_id);


--
-- TOC entry 4012 (class 1259 OID 21119)
-- Name: idx_dataset_platform; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_platform ON public.dataset USING btree (fk_platform_id);


--
-- TOC entry 4013 (class 1259 OID 21114)
-- Name: idx_dataset_procedure; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_procedure ON public.dataset USING btree (fk_procedure_id);


--
-- TOC entry 4034 (class 1259 OID 21140)
-- Name: idx_dataset_reference_to; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_reference_to ON public.dataset_reference USING btree (fk_dataset_id_to);


--
-- TOC entry 4014 (class 1259 OID 21113)
-- Name: idx_dataset_staidentifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_staidentifier ON public.dataset USING btree (sta_identifier);


--
-- TOC entry 4015 (class 1259 OID 21120)
-- Name: idx_dataset_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_unit ON public.dataset USING btree (fk_unit_id);


--
-- TOC entry 4016 (class 1259 OID 21129)
-- Name: idx_dataset_value_profile; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_value_profile ON public.dataset USING btree (fk_value_profile_id);


--
-- TOC entry 4017 (class 1259 OID 21126)
-- Name: idx_dataset_value_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dataset_value_type ON public.dataset USING btree (value_type);


--
-- TOC entry 4195 (class 1259 OID 21248)
-- Name: idx_end_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_end_time ON public.procedure_history USING btree (valid_to);


--
-- TOC entry 4050 (class 1259 OID 21152)
-- Name: idx_feature_child; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_child ON public.feature_hierarchy USING btree (fk_child_feature_id);


--
-- TOC entry 4037 (class 1259 OID 21141)
-- Name: idx_feature_format; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_format ON public.feature USING btree (fk_format_id);


--
-- TOC entry 4053 (class 1259 OID 21153)
-- Name: idx_feature_i18n_feature; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_i18n_feature ON public.feature_i18n USING btree (fk_feature_id);


--
-- TOC entry 4038 (class 1259 OID 21142)
-- Name: idx_feature_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_identifier ON public.feature USING btree (identifier);


--
-- TOC entry 4039 (class 1259 OID 21144)
-- Name: idx_feature_identifier_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_identifier_codespace ON public.feature USING btree (fk_identifier_codespace_id);


--
-- TOC entry 4040 (class 1259 OID 21145)
-- Name: idx_feature_name_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_name_codespace ON public.feature USING btree (fk_name_codespace_id);


--
-- TOC entry 4056 (class 1259 OID 21154)
-- Name: idx_feature_param_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_param_name ON public.feature_parameter USING btree (name);


--
-- TOC entry 4057 (class 1259 OID 21155)
-- Name: idx_feature_parameter; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_parameter ON public.feature_parameter USING btree (fk_feature_id);


--
-- TOC entry 4058 (class 1259 OID 21156)
-- Name: idx_feature_parameter_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_parameter_unit ON public.feature_parameter USING btree (fk_unit_id);


--
-- TOC entry 4041 (class 1259 OID 21143)
-- Name: idx_feature_staidentifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_feature_staidentifier ON public.feature USING btree (sta_identifier);


--
-- TOC entry 4065 (class 1259 OID 21161)
-- Name: idx_historical_location_platform; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_historical_location_platform ON public.historical_location USING btree (fk_platform_id);


--
-- TOC entry 4066 (class 1259 OID 21159)
-- Name: idx_historicallocation_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_historicallocation_identifier ON public.historical_location USING btree (identifier);


--
-- TOC entry 4067 (class 1259 OID 21160)
-- Name: idx_historicallocation_staidentifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_historicallocation_staidentifier ON public.historical_location USING btree (sta_identifier);


--
-- TOC entry 4072 (class 1259 OID 21168)
-- Name: idx_location_format; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_format ON public.location USING btree (fk_format_id);


--
-- TOC entry 4081 (class 1259 OID 21173)
-- Name: idx_location_historical_location; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_historical_location ON public.location_historical_location USING btree (fk_historical_location_id);


--
-- TOC entry 4084 (class 1259 OID 21174)
-- Name: idx_location_i18n_location; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_i18n_location ON public.location_i18n USING btree (fk_location_id);


--
-- TOC entry 4073 (class 1259 OID 21166)
-- Name: idx_location_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_identifier ON public.location USING btree (identifier);


--
-- TOC entry 4087 (class 1259 OID 21175)
-- Name: idx_location_param_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_param_name ON public.location_parameter USING btree (name);


--
-- TOC entry 4088 (class 1259 OID 21176)
-- Name: idx_location_parameter; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_parameter ON public.location_parameter USING btree (fk_location_id);


--
-- TOC entry 4089 (class 1259 OID 21177)
-- Name: idx_location_parameter_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_parameter_unit ON public.location_parameter USING btree (fk_unit_id);


--
-- TOC entry 4171 (class 1259 OID 21230)
-- Name: idx_location_platform; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_platform ON public.platform_location USING btree (fk_location_id);


--
-- TOC entry 4074 (class 1259 OID 21167)
-- Name: idx_location_staidentifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_location_staidentifier ON public.location USING btree (sta_identifier);


--
-- TOC entry 4092 (class 1259 OID 21178)
-- Name: idx_observation_dataset; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_dataset ON public.observation USING btree (fk_dataset_id);


--
-- TOC entry 4110 (class 1259 OID 21194)
-- Name: idx_observation_i18n_observation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_i18n_observation ON public.observation_i18n USING btree (fk_observation_id);


--
-- TOC entry 4093 (class 1259 OID 21183)
-- Name: idx_observation_identifier_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_identifier_codespace ON public.observation USING btree (fk_identifier_codespace_id);


--
-- TOC entry 4094 (class 1259 OID 21185)
-- Name: idx_observation_is_deleted; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_is_deleted ON public.observation USING btree (is_deleted);


--
-- TOC entry 4095 (class 1259 OID 21184)
-- Name: idx_observation_name_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_name_codespace ON public.observation USING btree (fk_name_codespace_id);


--
-- TOC entry 4113 (class 1259 OID 21195)
-- Name: idx_observation_param_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_param_name ON public.observation_parameter USING btree (name);


--
-- TOC entry 4114 (class 1259 OID 21196)
-- Name: idx_observation_parameter; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_parameter ON public.observation_parameter USING btree (fk_observation_id);


--
-- TOC entry 4115 (class 1259 OID 21197)
-- Name: idx_observation_parameter_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_parameter_unit ON public.observation_parameter USING btree (fk_unit_id);


--
-- TOC entry 4096 (class 1259 OID 21186)
-- Name: idx_observation_parent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_parent ON public.observation USING btree (fk_parent_observation_id);


--
-- TOC entry 4097 (class 1259 OID 21187)
-- Name: idx_observation_result_template; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_result_template ON public.observation USING btree (fk_result_template_id);


--
-- TOC entry 4098 (class 1259 OID 21182)
-- Name: idx_observation_staidentifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_observation_staidentifier ON public.observation USING btree (sta_identifier);


--
-- TOC entry 4128 (class 1259 OID 21204)
-- Name: idx_offering_child_offering; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offering_child_offering ON public.offering_hierarchy USING btree (fk_child_offering_id);


--
-- TOC entry 4125 (class 1259 OID 21203)
-- Name: idx_offering_feature_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offering_feature_type ON public.offering_feature_type USING btree (fk_format_id);


--
-- TOC entry 4131 (class 1259 OID 21205)
-- Name: idx_offering_i18n_offering; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offering_i18n_offering ON public.offering_i18n USING btree (fk_offering_id);


--
-- TOC entry 4118 (class 1259 OID 21198)
-- Name: idx_offering_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offering_identifier ON public.offering USING btree (identifier);


--
-- TOC entry 4119 (class 1259 OID 21199)
-- Name: idx_offering_identifier_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offering_identifier_codespace ON public.offering USING btree (fk_identifier_codespace_id);


--
-- TOC entry 4120 (class 1259 OID 21200)
-- Name: idx_offering_name_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offering_name_codespace ON public.offering USING btree (fk_name_codespace_id);


--
-- TOC entry 4134 (class 1259 OID 21206)
-- Name: idx_offering_observation_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offering_observation_type ON public.offering_observation_type USING btree (fk_format_id);


--
-- TOC entry 4137 (class 1259 OID 21207)
-- Name: idx_offering_related_feature; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_offering_related_feature ON public.offering_related_feature USING btree (fk_related_feature_id);


--
-- TOC entry 3997 (class 1259 OID 21111)
-- Name: idx_phenomenon_child_phenomenon; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_child_phenomenon ON public.composite_phenomenon USING btree (fk_child_phenomenon_id);


--
-- TOC entry 4150 (class 1259 OID 21216)
-- Name: idx_phenomenon_i18n_phenomenon; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_i18n_phenomenon ON public.phenomenon_i18n USING btree (fk_phenomenon_id);


--
-- TOC entry 4140 (class 1259 OID 21208)
-- Name: idx_phenomenon_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_identifier ON public.phenomenon USING btree (identifier);


--
-- TOC entry 4141 (class 1259 OID 21210)
-- Name: idx_phenomenon_identifier_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_identifier_codespace ON public.phenomenon USING btree (fk_identifier_codespace_id);


--
-- TOC entry 4142 (class 1259 OID 21211)
-- Name: idx_phenomenon_name_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_name_codespace ON public.phenomenon USING btree (fk_name_codespace_id);


--
-- TOC entry 4153 (class 1259 OID 21217)
-- Name: idx_phenomenon_param_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_param_name ON public.phenomenon_parameter USING btree (name);


--
-- TOC entry 4154 (class 1259 OID 21218)
-- Name: idx_phenomenon_parameter; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_parameter ON public.phenomenon_parameter USING btree (fk_phenomenon_id);


--
-- TOC entry 4155 (class 1259 OID 21219)
-- Name: idx_phenomenon_parameter_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_parameter_unit ON public.phenomenon_parameter USING btree (fk_unit_id);


--
-- TOC entry 4143 (class 1259 OID 21209)
-- Name: idx_phenomenon_staidentifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_phenomenon_staidentifier ON public.phenomenon USING btree (sta_identifier);


--
-- TOC entry 4168 (class 1259 OID 21228)
-- Name: idx_platform_i18n_platform; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_i18n_platform ON public.platform_i18n USING btree (fk_platform_id);


--
-- TOC entry 4158 (class 1259 OID 21220)
-- Name: idx_platform_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_identifier ON public.platform USING btree (identifier);


--
-- TOC entry 4159 (class 1259 OID 21222)
-- Name: idx_platform_identifier_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_identifier_codespace ON public.platform USING btree (fk_identifier_codespace_id);


--
-- TOC entry 4172 (class 1259 OID 21229)
-- Name: idx_platform_location; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_location ON public.platform_location USING btree (fk_platform_id);


--
-- TOC entry 4160 (class 1259 OID 21223)
-- Name: idx_platform_name_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_name_codespace ON public.platform USING btree (fk_name_codespace_id);


--
-- TOC entry 4175 (class 1259 OID 21231)
-- Name: idx_platform_param_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_param_name ON public.platform_parameter USING btree (name);


--
-- TOC entry 4176 (class 1259 OID 21232)
-- Name: idx_platform_parameter; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_parameter ON public.platform_parameter USING btree (fk_platform_id);


--
-- TOC entry 4177 (class 1259 OID 21233)
-- Name: idx_platform_parameter_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_parameter_unit ON public.platform_parameter USING btree (fk_unit_id);


--
-- TOC entry 4161 (class 1259 OID 21221)
-- Name: idx_platform_staidentifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_platform_staidentifier ON public.platform USING btree (sta_identifier);


--
-- TOC entry 4192 (class 1259 OID 21244)
-- Name: idx_procedure_child; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_child ON public.procedure_hierarchy USING btree (fk_child_procedure_id);


--
-- TOC entry 4180 (class 1259 OID 21239)
-- Name: idx_procedure_format; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_format ON public.procedure USING btree (fk_format_id);


--
-- TOC entry 4196 (class 1259 OID 21246)
-- Name: idx_procedure_history_format; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_history_format ON public.procedure_history USING btree (fk_format_id);


--
-- TOC entry 4197 (class 1259 OID 21245)
-- Name: idx_procedure_history_procedure; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_history_procedure ON public.procedure_history USING btree (fk_procedure_id);


--
-- TOC entry 4201 (class 1259 OID 21249)
-- Name: idx_procedure_i18n_procedure; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_i18n_procedure ON public.procedure_i18n USING btree (fk_procedure_id);


--
-- TOC entry 4181 (class 1259 OID 21234)
-- Name: idx_procedure_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_identifier ON public.procedure USING btree (identifier);


--
-- TOC entry 4182 (class 1259 OID 21236)
-- Name: idx_procedure_identifier_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_identifier_codespace ON public.procedure USING btree (fk_identifier_codespace_id);


--
-- TOC entry 4183 (class 1259 OID 21237)
-- Name: idx_procedure_name_codespace; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_name_codespace ON public.procedure USING btree (fk_name_codespace_id);


--
-- TOC entry 4204 (class 1259 OID 21250)
-- Name: idx_procedure_param_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_param_name ON public.procedure_parameter USING btree (name);


--
-- TOC entry 4205 (class 1259 OID 21251)
-- Name: idx_procedure_parameter; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_parameter ON public.procedure_parameter USING btree (fk_procedure_id);


--
-- TOC entry 4206 (class 1259 OID 21252)
-- Name: idx_procedure_parameter_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_parameter_unit ON public.procedure_parameter USING btree (fk_unit_id);


--
-- TOC entry 4184 (class 1259 OID 21235)
-- Name: idx_procedure_staidentifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_staidentifier ON public.procedure USING btree (sta_identifier);


--
-- TOC entry 4185 (class 1259 OID 21238)
-- Name: idx_procedure_type_of; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedure_type_of ON public.procedure USING btree (fk_type_of_procedure_id);


--
-- TOC entry 4244 (class 1259 OID 21268)
-- Name: idx_profile_vertical_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_profile_vertical_unit ON public.value_profile USING btree (fk_vertical_unit_id);


--
-- TOC entry 4209 (class 1259 OID 21253)
-- Name: idx_related_dataset_related_dataset; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_related_dataset_related_dataset ON public.related_dataset USING btree (fk_related_dataset_id);


--
-- TOC entry 4212 (class 1259 OID 21254)
-- Name: idx_related_feature_feature; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_related_feature_feature ON public.related_feature USING btree (fk_feature_id);


--
-- TOC entry 4215 (class 1259 OID 21256)
-- Name: idx_related_observation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_related_observation ON public.related_observation USING btree (fk_observation_id);


--
-- TOC entry 4216 (class 1259 OID 21255)
-- Name: idx_related_observation_related_observation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_related_observation_related_observation ON public.related_observation USING btree (fk_related_observation_id);


--
-- TOC entry 4219 (class 1259 OID 21261)
-- Name: idx_result_template_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_result_template_category ON public.result_template USING btree (fk_category_id);


--
-- TOC entry 4220 (class 1259 OID 21260)
-- Name: idx_result_template_feature; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_result_template_feature ON public.result_template USING btree (fk_feature_id);


--
-- TOC entry 4221 (class 1259 OID 21262)
-- Name: idx_result_template_identifier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_result_template_identifier ON public.result_template USING btree (identifier);


--
-- TOC entry 4222 (class 1259 OID 21257)
-- Name: idx_result_template_offering; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_result_template_offering ON public.result_template USING btree (fk_offering_id);


--
-- TOC entry 4223 (class 1259 OID 21258)
-- Name: idx_result_template_phenomenon; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_result_template_phenomenon ON public.result_template USING btree (fk_phenomenon_id);


--
-- TOC entry 4224 (class 1259 OID 21259)
-- Name: idx_result_template_procedure; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_result_template_procedure ON public.result_template USING btree (fk_procedure_id);


--
-- TOC entry 4099 (class 1259 OID 21181)
-- Name: idx_result_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_result_time ON public.observation USING btree (result_time);


--
-- TOC entry 4100 (class 1259 OID 21180)
-- Name: idx_sampling_time_end; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sampling_time_end ON public.observation USING btree (sampling_time_end);


--
-- TOC entry 4101 (class 1259 OID 21179)
-- Name: idx_sampling_time_start; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sampling_time_start ON public.observation USING btree (sampling_time_start);


--
-- TOC entry 4198 (class 1259 OID 21247)
-- Name: idx_start_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_start_time ON public.procedure_history USING btree (valid_from);


--
-- TOC entry 4229 (class 1259 OID 21263)
-- Name: idx_tag_dataset_dataset; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tag_dataset_dataset ON public.tag_dataset USING btree (fk_dataset_id);


--
-- TOC entry 4232 (class 1259 OID 21264)
-- Name: idx_tag_i18n_tag; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tag_i18n_tag ON public.tag_i18n USING btree (fk_tag_id);


--
-- TOC entry 4239 (class 1259 OID 21267)
-- Name: idx_unit_i18n_unit; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_unit_i18n_unit ON public.unit_i18n USING btree (fk_unit_id);


--
-- TOC entry 4247 (class 1259 OID 21269)
-- Name: idx_value_profile_i18n_value_profile; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_value_profile_i18n_value_profile ON public.value_profile_i18n USING btree (fk_value_profile_id);


--
-- TOC entry 4261 (class 2606 OID 21387)
-- Name: dataset fk_aggregation_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_aggregation_id FOREIGN KEY (fk_aggregation_id) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4350 (class 2606 OID 21832)
-- Name: value_blob fk_blob_value; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.value_blob
    ADD CONSTRAINT fk_blob_value FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);


--
-- TOC entry 4250 (class 2606 OID 21332)
-- Name: category_i18n fk_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_i18n
    ADD CONSTRAINT fk_category FOREIGN KEY (fk_category_id) REFERENCES public.category(category_id);


--
-- TOC entry 4291 (class 2606 OID 21537)
-- Name: observation fk_data_identifier_codesp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_data_identifier_codesp FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4292 (class 2606 OID 21542)
-- Name: observation fk_data_name_codespace; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_data_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4290 (class 2606 OID 21532)
-- Name: observation fk_dataset; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_dataset FOREIGN KEY (fk_dataset_id) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4256 (class 2606 OID 21362)
-- Name: dataset fk_dataset_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_category FOREIGN KEY (fk_category_id) REFERENCES public.category(category_id);


--
-- TOC entry 4257 (class 2606 OID 21367)
-- Name: dataset fk_dataset_feature; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_feature FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);


--
-- TOC entry 4262 (class 2606 OID 21392)
-- Name: dataset fk_dataset_first_obs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_first_obs FOREIGN KEY (fk_first_observation_id) REFERENCES public.observation(observation_id);


--
-- TOC entry 4267 (class 2606 OID 21417)
-- Name: dataset_i18n fk_dataset_i18n; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_i18n
    ADD CONSTRAINT fk_dataset_i18n FOREIGN KEY (fk_dataset_id) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4264 (class 2606 OID 21402)
-- Name: dataset fk_dataset_identifier_codesp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_identifier_codesp FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4263 (class 2606 OID 21397)
-- Name: dataset fk_dataset_last_obs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_last_obs FOREIGN KEY (fk_last_observation_id) REFERENCES public.observation(observation_id);


--
-- TOC entry 4265 (class 2606 OID 21407)
-- Name: dataset fk_dataset_name_codespace; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4255 (class 2606 OID 21357)
-- Name: dataset fk_dataset_offering; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_offering FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);


--
-- TOC entry 4260 (class 2606 OID 21382)
-- Name: dataset fk_dataset_om_obs_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_om_obs_type FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);


--
-- TOC entry 4269 (class 2606 OID 21427)
-- Name: dataset_parameter fk_dataset_param_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_parameter
    ADD CONSTRAINT fk_dataset_param_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4268 (class 2606 OID 21422)
-- Name: dataset_parameter fk_dataset_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_parameter
    ADD CONSTRAINT fk_dataset_parameter FOREIGN KEY (fk_dataset_id) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4270 (class 2606 OID 21432)
-- Name: dataset_parameter fk_dataset_parent_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_parameter
    ADD CONSTRAINT fk_dataset_parent_parameter FOREIGN KEY (fk_parent_parameter_id) REFERENCES public.dataset_parameter(parameter_id);


--
-- TOC entry 4254 (class 2606 OID 21352)
-- Name: dataset fk_dataset_phenomenon; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_phenomenon FOREIGN KEY (fk_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);


--
-- TOC entry 4258 (class 2606 OID 21372)
-- Name: dataset fk_dataset_platform; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_platform FOREIGN KEY (fk_platform_id) REFERENCES public.platform(platform_id);


--
-- TOC entry 4253 (class 2606 OID 21347)
-- Name: dataset fk_dataset_procedure; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_procedure FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);


--
-- TOC entry 4272 (class 2606 OID 21442)
-- Name: dataset_reference fk_dataset_reference_from; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_reference
    ADD CONSTRAINT fk_dataset_reference_from FOREIGN KEY (fk_dataset_id_from) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4271 (class 2606 OID 21437)
-- Name: dataset_reference fk_dataset_reference_to; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset_reference
    ADD CONSTRAINT fk_dataset_reference_to FOREIGN KEY (fk_dataset_id_to) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4347 (class 2606 OID 21817)
-- Name: tag_dataset fk_dataset_tag; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag_dataset
    ADD CONSTRAINT fk_dataset_tag FOREIGN KEY (fk_dataset_id) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4259 (class 2606 OID 21377)
-- Name: dataset fk_dataset_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_dataset_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4278 (class 2606 OID 21472)
-- Name: feature_i18n fk_feature; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_i18n
    ADD CONSTRAINT fk_feature FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);


--
-- TOC entry 4277 (class 2606 OID 21467)
-- Name: feature_hierarchy fk_feature_child; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_hierarchy
    ADD CONSTRAINT fk_feature_child FOREIGN KEY (fk_child_feature_id) REFERENCES public.feature(feature_id);


--
-- TOC entry 4273 (class 2606 OID 21447)
-- Name: feature fk_feature_format; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature
    ADD CONSTRAINT fk_feature_format FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);


--
-- TOC entry 4274 (class 2606 OID 21452)
-- Name: feature fk_feature_identifier_codesp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature
    ADD CONSTRAINT fk_feature_identifier_codesp FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4275 (class 2606 OID 21457)
-- Name: feature fk_feature_name_codespace; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature
    ADD CONSTRAINT fk_feature_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4280 (class 2606 OID 21482)
-- Name: feature_parameter fk_feature_param_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_parameter
    ADD CONSTRAINT fk_feature_param_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4279 (class 2606 OID 21477)
-- Name: feature_parameter fk_feature_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_parameter
    ADD CONSTRAINT fk_feature_parameter FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);


--
-- TOC entry 4276 (class 2606 OID 21462)
-- Name: feature_hierarchy fk_feature_parent; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_hierarchy
    ADD CONSTRAINT fk_feature_parent FOREIGN KEY (fk_parent_feature_id) REFERENCES public.feature(feature_id);


--
-- TOC entry 4281 (class 2606 OID 21487)
-- Name: feature_parameter fk_feature_parent_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feature_parameter
    ADD CONSTRAINT fk_feature_parent_parameter FOREIGN KEY (fk_parent_parameter_id) REFERENCES public.feature_parameter(parameter_id);


--
-- TOC entry 4301 (class 2606 OID 21587)
-- Name: offering_feature_type fk_feature_type_offering; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_feature_type
    ADD CONSTRAINT fk_feature_type_offering FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);


--
-- TOC entry 4284 (class 2606 OID 21502)
-- Name: location_historical_location fk_historical_loc_location; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_historical_location
    ADD CONSTRAINT fk_historical_loc_location FOREIGN KEY (fk_historical_location_id) REFERENCES public.historical_location(historical_location_id);


--
-- TOC entry 4332 (class 2606 OID 21742)
-- Name: procedure_i18n fk_i18n_procedure; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_i18n
    ADD CONSTRAINT fk_i18n_procedure FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);


--
-- TOC entry 4286 (class 2606 OID 21512)
-- Name: location_i18n fk_location; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_i18n
    ADD CONSTRAINT fk_location FOREIGN KEY (fk_location_id) REFERENCES public.location(location_id);


--
-- TOC entry 4283 (class 2606 OID 21497)
-- Name: location fk_location_format; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location
    ADD CONSTRAINT fk_location_format FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);


--
-- TOC entry 4285 (class 2606 OID 21507)
-- Name: location_historical_location fk_location_historical_loc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_historical_location
    ADD CONSTRAINT fk_location_historical_loc FOREIGN KEY (fk_location_id) REFERENCES public.location(location_id);


--
-- TOC entry 4288 (class 2606 OID 21522)
-- Name: location_parameter fk_location_param_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_parameter
    ADD CONSTRAINT fk_location_param_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4287 (class 2606 OID 21517)
-- Name: location_parameter fk_location_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_parameter
    ADD CONSTRAINT fk_location_parameter FOREIGN KEY (fk_location_id) REFERENCES public.location(location_id);


--
-- TOC entry 4289 (class 2606 OID 21527)
-- Name: location_parameter fk_location_parent_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.location_parameter
    ADD CONSTRAINT fk_location_parent_parameter FOREIGN KEY (fk_parent_parameter_id) REFERENCES public.location_parameter(parameter_id);


--
-- TOC entry 4320 (class 2606 OID 21682)
-- Name: platform_location fk_location_platform; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_location
    ADD CONSTRAINT fk_location_platform FOREIGN KEY (fk_location_id) REFERENCES public.location(location_id);


--
-- TOC entry 4295 (class 2606 OID 21557)
-- Name: observation_i18n fk_observation_i18n; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation_i18n
    ADD CONSTRAINT fk_observation_i18n FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);


--
-- TOC entry 4297 (class 2606 OID 21567)
-- Name: observation_parameter fk_observation_param_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation_parameter
    ADD CONSTRAINT fk_observation_param_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4296 (class 2606 OID 21562)
-- Name: observation_parameter fk_observation_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation_parameter
    ADD CONSTRAINT fk_observation_parameter FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);


--
-- TOC entry 4298 (class 2606 OID 21572)
-- Name: observation_parameter fk_observation_parent_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation_parameter
    ADD CONSTRAINT fk_observation_parent_parameter FOREIGN KEY (fk_parent_parameter_id) REFERENCES public.observation_parameter(parameter_id);


--
-- TOC entry 4306 (class 2606 OID 21612)
-- Name: offering_observation_type fk_observation_type_offering; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_observation_type
    ADD CONSTRAINT fk_observation_type_offering FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);


--
-- TOC entry 4305 (class 2606 OID 21607)
-- Name: offering_i18n fk_offering; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_i18n
    ADD CONSTRAINT fk_offering FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);


--
-- TOC entry 4304 (class 2606 OID 21602)
-- Name: offering_hierarchy fk_offering_child; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_hierarchy
    ADD CONSTRAINT fk_offering_child FOREIGN KEY (fk_child_offering_id) REFERENCES public.offering(offering_id);


--
-- TOC entry 4302 (class 2606 OID 21592)
-- Name: offering_feature_type fk_offering_feature_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_feature_type
    ADD CONSTRAINT fk_offering_feature_type FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);


--
-- TOC entry 4299 (class 2606 OID 21577)
-- Name: offering fk_offering_identifier_codesp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering
    ADD CONSTRAINT fk_offering_identifier_codesp FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4300 (class 2606 OID 21582)
-- Name: offering fk_offering_name_codespace; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering
    ADD CONSTRAINT fk_offering_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4307 (class 2606 OID 21617)
-- Name: offering_observation_type fk_offering_observation_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_observation_type
    ADD CONSTRAINT fk_offering_observation_type FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);


--
-- TOC entry 4303 (class 2606 OID 21597)
-- Name: offering_hierarchy fk_offering_parent; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_hierarchy
    ADD CONSTRAINT fk_offering_parent FOREIGN KEY (fk_parent_offering_id) REFERENCES public.offering(offering_id);


--
-- TOC entry 4308 (class 2606 OID 21622)
-- Name: offering_related_feature fk_offering_related_feature; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_related_feature
    ADD CONSTRAINT fk_offering_related_feature FOREIGN KEY (fk_related_feature_id) REFERENCES public.related_feature(related_feature_id);


--
-- TOC entry 4294 (class 2606 OID 21552)
-- Name: observation fk_parent_observation; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_parent_observation FOREIGN KEY (fk_parent_observation_id) REFERENCES public.observation(observation_id);


--
-- TOC entry 4331 (class 2606 OID 21737)
-- Name: procedure_history fk_pdf_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_history
    ADD CONSTRAINT fk_pdf_id FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);


--
-- TOC entry 4312 (class 2606 OID 21642)
-- Name: phenomenon_i18n fk_phenomenon; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon_i18n
    ADD CONSTRAINT fk_phenomenon FOREIGN KEY (fk_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);


--
-- TOC entry 4252 (class 2606 OID 21342)
-- Name: composite_phenomenon fk_phenomenon_child; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.composite_phenomenon
    ADD CONSTRAINT fk_phenomenon_child FOREIGN KEY (fk_child_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);


--
-- TOC entry 4310 (class 2606 OID 21632)
-- Name: phenomenon fk_phenomenon_identifier_codesp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT fk_phenomenon_identifier_codesp FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4311 (class 2606 OID 21637)
-- Name: phenomenon fk_phenomenon_name_codespace; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon
    ADD CONSTRAINT fk_phenomenon_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4314 (class 2606 OID 21652)
-- Name: phenomenon_parameter fk_phenomenon_param_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon_parameter
    ADD CONSTRAINT fk_phenomenon_param_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4313 (class 2606 OID 21647)
-- Name: phenomenon_parameter fk_phenomenon_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon_parameter
    ADD CONSTRAINT fk_phenomenon_parameter FOREIGN KEY (fk_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);


--
-- TOC entry 4251 (class 2606 OID 21337)
-- Name: composite_phenomenon fk_phenomenon_parent; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.composite_phenomenon
    ADD CONSTRAINT fk_phenomenon_parent FOREIGN KEY (fk_parent_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);


--
-- TOC entry 4315 (class 2606 OID 21657)
-- Name: phenomenon_parameter fk_phenomenon_parent_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phenomenon_parameter
    ADD CONSTRAINT fk_phenomenon_parent_parameter FOREIGN KEY (fk_parent_parameter_id) REFERENCES public.phenomenon_parameter(parameter_id);


--
-- TOC entry 4318 (class 2606 OID 21672)
-- Name: platform_i18n fk_platform; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_i18n
    ADD CONSTRAINT fk_platform FOREIGN KEY (fk_platform_id) REFERENCES public.platform(platform_id);


--
-- TOC entry 4282 (class 2606 OID 21492)
-- Name: historical_location fk_platform_historical_location; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historical_location
    ADD CONSTRAINT fk_platform_historical_location FOREIGN KEY (fk_platform_id) REFERENCES public.platform(platform_id);


--
-- TOC entry 4316 (class 2606 OID 21662)
-- Name: platform fk_platform_identifier_codesp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform
    ADD CONSTRAINT fk_platform_identifier_codesp FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4319 (class 2606 OID 21677)
-- Name: platform_location fk_platform_location; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_location
    ADD CONSTRAINT fk_platform_location FOREIGN KEY (fk_platform_id) REFERENCES public.platform(platform_id);


--
-- TOC entry 4317 (class 2606 OID 21667)
-- Name: platform fk_platform_name_codespace; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform
    ADD CONSTRAINT fk_platform_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4322 (class 2606 OID 21692)
-- Name: platform_parameter fk_platform_param_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_parameter
    ADD CONSTRAINT fk_platform_param_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4321 (class 2606 OID 21687)
-- Name: platform_parameter fk_platform_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_parameter
    ADD CONSTRAINT fk_platform_parameter FOREIGN KEY (fk_platform_id) REFERENCES public.platform(platform_id);


--
-- TOC entry 4323 (class 2606 OID 21697)
-- Name: platform_parameter fk_platform_parent_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.platform_parameter
    ADD CONSTRAINT fk_platform_parent_parameter FOREIGN KEY (fk_parent_parameter_id) REFERENCES public.platform_parameter(parameter_id);


--
-- TOC entry 4329 (class 2606 OID 21727)
-- Name: procedure_hierarchy fk_procedure_child; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_hierarchy
    ADD CONSTRAINT fk_procedure_child FOREIGN KEY (fk_child_procedure_id) REFERENCES public.procedure(procedure_id);


--
-- TOC entry 4327 (class 2606 OID 21717)
-- Name: procedure fk_procedure_format; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT fk_procedure_format FOREIGN KEY (fk_format_id) REFERENCES public.format(format_id);


--
-- TOC entry 4330 (class 2606 OID 21732)
-- Name: procedure_history fk_procedure_history; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_history
    ADD CONSTRAINT fk_procedure_history FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);


--
-- TOC entry 4324 (class 2606 OID 21702)
-- Name: procedure fk_procedure_identifier_codesp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT fk_procedure_identifier_codesp FOREIGN KEY (fk_identifier_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4325 (class 2606 OID 21707)
-- Name: procedure fk_procedure_name_codespace; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT fk_procedure_name_codespace FOREIGN KEY (fk_name_codespace_id) REFERENCES public.codespace(codespace_id);


--
-- TOC entry 4334 (class 2606 OID 21752)
-- Name: procedure_parameter fk_procedure_param_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_parameter
    ADD CONSTRAINT fk_procedure_param_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4333 (class 2606 OID 21747)
-- Name: procedure_parameter fk_procedure_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_parameter
    ADD CONSTRAINT fk_procedure_parameter FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);


--
-- TOC entry 4328 (class 2606 OID 21722)
-- Name: procedure_hierarchy fk_procedure_parent; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_hierarchy
    ADD CONSTRAINT fk_procedure_parent FOREIGN KEY (fk_parent_procedure_id) REFERENCES public.procedure(procedure_id);


--
-- TOC entry 4335 (class 2606 OID 21757)
-- Name: procedure_parameter fk_procedure_parent_parameter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure_parameter
    ADD CONSTRAINT fk_procedure_parent_parameter FOREIGN KEY (fk_parent_parameter_id) REFERENCES public.procedure_parameter(parameter_id);


--
-- TOC entry 4351 (class 2606 OID 21837)
-- Name: value_profile fk_profile_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.value_profile
    ADD CONSTRAINT fk_profile_unit FOREIGN KEY (fk_vertical_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4336 (class 2606 OID 21762)
-- Name: related_dataset fk_rel_dataset_dataset; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.related_dataset
    ADD CONSTRAINT fk_rel_dataset_dataset FOREIGN KEY (fk_dataset_id) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4337 (class 2606 OID 21767)
-- Name: related_dataset fk_rel_dataset_rel_dataset; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.related_dataset
    ADD CONSTRAINT fk_rel_dataset_rel_dataset FOREIGN KEY (fk_related_dataset_id) REFERENCES public.dataset(dataset_id);


--
-- TOC entry 4340 (class 2606 OID 21782)
-- Name: related_observation fk_rel_obs_related; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.related_observation
    ADD CONSTRAINT fk_rel_obs_related FOREIGN KEY (fk_related_observation_id) REFERENCES public.observation(observation_id);


--
-- TOC entry 4338 (class 2606 OID 21772)
-- Name: related_feature fk_related_feature; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.related_feature
    ADD CONSTRAINT fk_related_feature FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);


--
-- TOC entry 4309 (class 2606 OID 21627)
-- Name: offering_related_feature fk_related_feature_offering; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.offering_related_feature
    ADD CONSTRAINT fk_related_feature_offering FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);


--
-- TOC entry 4339 (class 2606 OID 21777)
-- Name: related_observation fk_related_observation; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.related_observation
    ADD CONSTRAINT fk_related_observation FOREIGN KEY (fk_observation_id) REFERENCES public.observation(observation_id);


--
-- TOC entry 4293 (class 2606 OID 21547)
-- Name: observation fk_result_template; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.observation
    ADD CONSTRAINT fk_result_template FOREIGN KEY (fk_result_template_id) REFERENCES public.result_template(result_template_id);


--
-- TOC entry 4345 (class 2606 OID 21807)
-- Name: result_template fk_result_template_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_category FOREIGN KEY (fk_category_id) REFERENCES public.category(category_id);


--
-- TOC entry 4344 (class 2606 OID 21802)
-- Name: result_template fk_result_template_feature; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_feature FOREIGN KEY (fk_feature_id) REFERENCES public.feature(feature_id);


--
-- TOC entry 4341 (class 2606 OID 21787)
-- Name: result_template fk_result_template_offering; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_offering FOREIGN KEY (fk_offering_id) REFERENCES public.offering(offering_id);


--
-- TOC entry 4342 (class 2606 OID 21792)
-- Name: result_template fk_result_template_phenomenon; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_phenomenon FOREIGN KEY (fk_phenomenon_id) REFERENCES public.phenomenon(phenomenon_id);


--
-- TOC entry 4343 (class 2606 OID 21797)
-- Name: result_template fk_result_template_procedure; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.result_template
    ADD CONSTRAINT fk_result_template_procedure FOREIGN KEY (fk_procedure_id) REFERENCES public.procedure(procedure_id);


--
-- TOC entry 4348 (class 2606 OID 21822)
-- Name: tag_i18n fk_tag; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag_i18n
    ADD CONSTRAINT fk_tag FOREIGN KEY (fk_tag_id) REFERENCES public.tag(tag_id);


--
-- TOC entry 4346 (class 2606 OID 21812)
-- Name: tag_dataset fk_tag_dataset; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tag_dataset
    ADD CONSTRAINT fk_tag_dataset FOREIGN KEY (fk_tag_id) REFERENCES public.tag(tag_id);


--
-- TOC entry 4326 (class 2606 OID 21712)
-- Name: procedure fk_type_of; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedure
    ADD CONSTRAINT fk_type_of FOREIGN KEY (fk_type_of_procedure_id) REFERENCES public.procedure(procedure_id);


--
-- TOC entry 4349 (class 2606 OID 21827)
-- Name: unit_i18n fk_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unit_i18n
    ADD CONSTRAINT fk_unit FOREIGN KEY (fk_unit_id) REFERENCES public.unit(unit_id);


--
-- TOC entry 4266 (class 2606 OID 21412)
-- Name: dataset fk_value_profile; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dataset
    ADD CONSTRAINT fk_value_profile FOREIGN KEY (fk_value_profile_id) REFERENCES public.value_profile(value_profile_id);


--
-- TOC entry 4352 (class 2606 OID 21842)
-- Name: value_profile_i18n fk_value_profile_i18n; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.value_profile_i18n
    ADD CONSTRAINT fk_value_profile_i18n FOREIGN KEY (fk_value_profile_id) REFERENCES public.value_profile(value_profile_id);


-- Completed on 2022-06-14 12:49:58 UTC

--
-- PostgreSQL database dump complete
--

