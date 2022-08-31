--
-- Test DATA
--
set session_replication_role to replica;

INSERT INTO public.category VALUES (1, 'DEFAULT_STA_CATEGORY', 'DEFAULT_STA_CATEGORY', 'Default STA category');

INSERT INTO public.dataset VALUES (1, NULL, '107dbe2e-c198-47ba-84cd-ae5b5ccd3905', 'b40e03cc-9249-4e1d-899b-1b3d4d865d27', 'datastream name 2', 'datastream 2', '2015-03-05 00:00:00', '2015-03-06 00:00:00', NULL, NULL, NULL, 1, 1, 1, 1, 1, 1, 1, 5, NULL, 5.0000000000, 6.0000000000, 2, 1, 'timeseries', 'simple', 'quantity', 0, 0, 1, 0, 1, 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.dataset VALUES (2, NULL, '144bb4aa-01c1-4111-8ad5-f27385a53661', 'accd5d6a-5443-4388-b480-d52931af746d', 'datastream name 1', 'datastream 1', '2015-03-03 00:00:00', '2015-03-04 00:00:00', NULL, NULL, NULL, 2, 2, 2, 1, 1, 1, 2, 5, NULL, 3.0000000000, 4.0000000000, 3, 4, 'timeseries', 'simple', 'quantity', 0, 0, 1, 0, 1, 0, NULL, NULL, NULL, NULL, NULL);

INSERT INTO public.feature VALUES (1, NULL, 10, 'a1865208-25c7-4b59-89b2-8fd0824188bb', 'a1865208-25c7-4b59-89b2-8fd0824188bb', NULL, 'location name 1', NULL, 'location 1', NULL, NULL, '0101000020E61000003333333333435DC06666666666864940');

INSERT INTO public.format VALUES (1, 'application/pdf');
INSERT INTO public.format VALUES (2, 'application/vnd.geo+json');
INSERT INTO public.format VALUES (3, 'http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation');
INSERT INTO public.format VALUES (4, 'http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation');
INSERT INTO public.format VALUES (5, 'http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement');
INSERT INTO public.format VALUES (6, 'http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation');
INSERT INTO public.format VALUES (7, 'http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation');
INSERT INTO public.format VALUES (8, 'http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingFeature');
INSERT INTO public.format VALUES (9, 'http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SpatialSamplingFeature');
INSERT INTO public.format VALUES (10, 'http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint');
INSERT INTO public.format VALUES (11, 'http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve');
INSERT INTO public.format VALUES (12, 'http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface');
INSERT INTO public.format VALUES (13, 'http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSolid');
INSERT INTO public.format VALUES (14, 'http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_Specimen');

INSERT INTO public.historical_location VALUES (1, '911cfe05-e395-46a2-aa9e-179eb99bdee6', '911cfe05-e395-46a2-aa9e-179eb99bdee6', 1, '2022-06-14 12:36:48.58');

INSERT INTO public.location VALUES (1, 'a1865208-25c7-4b59-89b2-8fd0824188bb', 'a1865208-25c7-4b59-89b2-8fd0824188bb', 'location name 1', 'location 1', NULL, '0101000020E61000003333333333435DC06666666666864940', 2);

INSERT INTO public.location_historical_location VALUES (1, 1);

INSERT INTO public.observation VALUES (1, 'quantity', 1, '2015-03-06 00:00:00', '2015-03-06 00:00:00', NULL, '817416f75de6224c953f9daf5e01597b7f0d2d7bc4a0015d16840973ef7798c8', '817416f75de6224c953f9daf5e01597b7f0d2d7bc4a0015d16840973ef7798c8', NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0.0000000000, 0.0000000000, NULL, 6.0000000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.observation VALUES (2, 'quantity', 1, '2015-03-05 00:00:00', '2015-03-05 00:00:00', NULL, 'bd375fcf3d2734bc94341c76908db4418c9e13ea4f31383b51c57cc96beb8018', 'bd375fcf3d2734bc94341c76908db4418c9e13ea4f31383b51c57cc96beb8018', NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0.0000000000, 0.0000000000, NULL, 5.0000000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.observation VALUES (3, 'quantity', 2, '2015-03-03 00:00:00', '2015-03-03 00:00:00', NULL, '3c32c7876ff74c946bbe29db0620d0099ad0db4505574c77830da174449e7059', '3c32c7876ff74c946bbe29db0620d0099ad0db4505574c77830da174449e7059', NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0.0000000000, 0.0000000000, NULL, 3.0000000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.observation VALUES (4, 'quantity', 2, '2015-03-04 00:00:00', '2015-03-04 00:00:00', NULL, 'bb6497430c4fa476715fa262151ace80e973cc092c870fadde4ce34054f8369f', 'bb6497430c4fa476715fa262151ace80e973cc092c870fadde4ce34054f8369f', NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0.0000000000, 0.0000000000, NULL, 4.0000000000, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);


INSERT INTO public.offering VALUES (1, '528c5604-c44b-4b47-9ea3-ae2015143881', NULL, 'sensor name 2', NULL, 'sensor 2', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.offering VALUES (2, '074d3742-ac12-40c1-871d-752798d93a04', NULL, 'sensor name 1', NULL, 'sensor 1', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO public.phenomenon VALUES (1, 'http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html/Tempreture', '8d5442f5-78c1-45cd-b215-3be8da4d8bc5', NULL, 'Tempretaure', NULL, 'observedProperty 2');
INSERT INTO public.phenomenon VALUES (2, 'http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html/LuminousFlux', '9129a885-4434-4936-969f-86d93075ef9b', NULL, 'Luminous Flux', NULL, 'observedProperty 1');

INSERT INTO public.platform VALUES (1, '04f4f620-7f2d-41f4-aacb-43c22aa6b5c6', '04f4f620-7f2d-41f4-aacb-43c22aa6b5c6', NULL, 'thing name 1', NULL, 'thing 1');

INSERT INTO public.platform_location VALUES (1, 1);

INSERT INTO public.platform_parameter VALUES (1, 'text', 'reference', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, 'first', NULL, NULL, NULL, NULL, NULL);

INSERT INTO public.procedure VALUES (1, '528c5604-c44b-4b47-9ea3-ae2015143881', '528c5604-c44b-4b47-9ea3-ae2015143881', NULL, 'sensor name 2', NULL, 'sensor 2', 'Tempreture sensor', 0, NULL, 0, 1);
INSERT INTO public.procedure VALUES (2, '074d3742-ac12-40c1-871d-752798d93a04', '074d3742-ac12-40c1-871d-752798d93a04', NULL, 'sensor name 1', NULL, 'sensor 1', 'Light flux sensor', 0, NULL, 0, 1);

INSERT INTO public.unit VALUES (1, 'C', 'Centigrade', 'http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen');
INSERT INTO public.unit VALUES (2, 'lm', 'Lumen', 'http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen');

set session_replication_role to default;
