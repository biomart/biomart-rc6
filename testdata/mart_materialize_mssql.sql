PRINT '1 - SELECT a.[seq_region_id] AS  ...';
SELECT a.[seq_region_id] AS seq_region_id_1021,a.[status] AS status_1021,a.[canonical_annotation] AS canonical_annotation_1021,a.[display_xref_id] AS display_xref_id_1021,a.[gene_id] AS gene_id_1021,a.[seq_region_end] AS seq_region_end_1021,a.[source] AS source_1021,a.[seq_region_start] AS seq_region_start_1021,a.[description] AS description_1021,a.[analysis_id] AS analysis_id_1021,a.[biotype] AS biotype_1021,a.[canonical_transcript_id] AS canonical_transcript_id_1021,a.[is_current] AS is_current_1021,a.[seq_region_strand] AS seq_region_strand_1021 INTO homo_sapiens_vega_58_37c.dbo.TEMP0 FROM homo_sapiens_vega_58_37c.dbo.gene AS a;
GO
;
PRINT '2 - CREATE INDEX I_0 ON homo_sap ...';
CREATE INDEX I_0 ON homo_sapiens_vega_58_37c.dbo.TEMP0(analysis_id_1021);
GO
;
PRINT '3 - SELECT a.*,b.[db] AS db_102, ...';
SELECT a.*,b.[db] AS db_102,b.[db_version] AS db_version_102,b.[module] AS module_102,b.[gff_source] AS gff_source_102,b.[module_version] AS module_version_102,b.[logic_name] AS logic_name_102,b.[gff_feature] AS gff_feature_102,b.[program_version] AS program_version_102,b.[db_file] AS db_file_102,b.[program_file] AS program_file_102,b.[created] AS created_102,b.[program] AS program_102,b.[parameters] AS parameters_102 INTO homo_sapiens_vega_58_37c.dbo.TEMP1 FROM homo_sapiens_vega_58_37c.dbo.TEMP0 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.analysis AS b ON a.[analysis_id_1021]=b.[analysis_id];
GO
;
PRINT '4 - DROP TABLE homo_sapiens_vega ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP0;
GO
;
PRINT '5 - CREATE INDEX I_1 ON homo_sap ...';
CREATE INDEX I_1 ON homo_sapiens_vega_58_37c.dbo.TEMP1(analysis_id_1021);
GO
;
PRINT '6 - SELECT a.*,b.[display_label] ...';
SELECT a.*,b.[display_label] AS display_label_103,b.[description] AS description_103,b.[displayable] AS displayable_103,b.[web_data] AS web_data_103 INTO homo_sapiens_vega_58_37c.dbo.TEMP2 FROM homo_sapiens_vega_58_37c.dbo.TEMP1 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.analysis_description AS b ON a.[analysis_id_1021]=b.[analysis_id];
GO
;
PRINT '7 - DROP TABLE homo_sapiens_vega ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP1;
GO
;
PRINT '8 - CREATE INDEX I_2 ON homo_sap ...';
CREATE INDEX I_2 ON homo_sapiens_vega_58_37c.dbo.TEMP2(gene_id_1021);
GO
;
PRINT '9 - SELECT a.*,b.[alt_allele_id] ...';
SELECT a.*,b.[alt_allele_id] AS alt_allele_id_101 INTO homo_sapiens_vega_58_37c.dbo.TEMP3 FROM homo_sapiens_vega_58_37c.dbo.TEMP2 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.alt_allele AS b ON a.[gene_id_1021]=b.[gene_id];
GO
;
PRINT '10 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP2;
GO
;
PRINT '11 - CREATE INDEX I_3 ON homo_sa ...';
CREATE INDEX I_3 ON homo_sapiens_vega_58_37c.dbo.TEMP3(gene_id_1021);
GO
;
PRINT '12 - SELECT a.*,b.[created_date] ...';
SELECT a.*,b.[created_date] AS created_date_1024,b.[stable_id] AS stable_id_1024,b.[modified_date] AS modified_date_1024,b.[version] AS version_1024 INTO homo_sapiens_vega_58_37c.dbo.TEMP4 FROM homo_sapiens_vega_58_37c.dbo.TEMP3 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.gene_stable_id AS b ON a.[gene_id_1021]=b.[gene_id];
GO
;
PRINT '13 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP3;
GO
;
PRINT '14 - CREATE INDEX I_4 ON homo_sa ...';
CREATE INDEX I_4 ON homo_sapiens_vega_58_37c.dbo.TEMP4(seq_region_id_1021);
GO
;
PRINT '15 - SELECT a.*,b.[name] AS name ...';
SELECT a.*,b.[name] AS name_1053,b.[length] AS length_1053,b.[coord_system_id] AS coord_system_id_1053 INTO homo_sapiens_vega_58_37c.dbo.TEMP5 FROM homo_sapiens_vega_58_37c.dbo.TEMP4 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.seq_region AS b ON a.[seq_region_id_1021]=b.[seq_region_id];
GO
;
PRINT '16 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP4;
GO
;
PRINT '17 - CREATE INDEX I_5 ON homo_sa ...';
CREATE INDEX I_5 ON homo_sapiens_vega_58_37c.dbo.TEMP5(coord_system_id_1053);
GO
;
PRINT '18 - SELECT a.*,b.[rank] AS rank ...';
SELECT a.*,b.[rank] AS rank_107,b.[name] AS name_107,b.[attrib] AS attrib_107,b.[species_id] AS species_id_107,b.[version] AS version_107 INTO homo_sapiens_vega_58_37c.dbo.TEMP6 FROM homo_sapiens_vega_58_37c.dbo.TEMP5 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.coord_system AS b ON a.[coord_system_id_1053]=b.[coord_system_id];
GO
;
PRINT '19 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP5;
GO
;
PRINT '20 - CREATE INDEX I_6 ON homo_sa ...';
CREATE INDEX I_6 ON homo_sapiens_vega_58_37c.dbo.TEMP6(seq_region_id_1021);
GO
;
PRINT '21 - SELECT a.*,b.[sequence] AS  ...';
SELECT a.*,b.[sequence] AS sequence_1013 INTO homo_sapiens_vega_58_37c.dbo.TEMP7 FROM homo_sapiens_vega_58_37c.dbo.TEMP6 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.dna AS b ON a.[seq_region_id_1021]=b.[seq_region_id];
GO
;
PRINT '22 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP6;
GO
;
PRINT '23 - CREATE INDEX I_7 ON homo_sa ...';
CREATE INDEX I_7 ON homo_sapiens_vega_58_37c.dbo.TEMP7(seq_region_id_1021);
GO
;
PRINT '24 - SELECT a.*,b.[sequence] AS  ...';
SELECT a.*,b.[sequence] AS sequence_1015,b.[n_line] AS n_line_1015 INTO homo_sapiens_vega_58_37c.dbo.TEMP8 FROM homo_sapiens_vega_58_37c.dbo.TEMP7 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.dnac AS b ON a.[seq_region_id_1021]=b.[seq_region_id];
GO
;
PRINT '25 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP7;
GO
;
PRINT '26 - EXEC SP_RENAME [dbo.TEMP8], ...';
EXEC SP_RENAME [dbo.TEMP8], [gene];
GO
;
PRINT '27 - CREATE INDEX I_8 ON homo_sa ...';
CREATE INDEX I_8 ON homo_sapiens_vega_58_37c.dbo.gene(gene_id_1021);
GO
;
PRINT '28 - SELECT a.[gene_id_1021] INT ...';
SELECT a.[gene_id_1021] INTO homo_sapiens_vega_58_37c.dbo.TEMP9 FROM homo_sapiens_vega_58_37c.dbo.gene AS a;
GO
;
PRINT '29 - CREATE INDEX I_9 ON homo_sa ...';
CREATE INDEX I_9 ON homo_sapiens_vega_58_37c.dbo.TEMP9(gene_id_1021);
GO
;
PRINT '30 - SELECT a.*,b.[value] AS val ...';
SELECT a.*,b.[value] AS value_1023,b.[attrib_type_id] AS attrib_type_id_1023 INTO homo_sapiens_vega_58_37c.dbo.TEMP10 FROM homo_sapiens_vega_58_37c.dbo.TEMP9 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.gene_attrib AS b ON a.[gene_id_1021]=b.[gene_id];
GO
;
PRINT '31 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP9;
GO
;
PRINT '32 - CREATE INDEX I_10 ON homo_s ...';
CREATE INDEX I_10 ON homo_sapiens_vega_58_37c.dbo.TEMP10(attrib_type_id_1023);
GO
;
PRINT '33 - SELECT a.*,b.[description]  ...';
SELECT a.*,b.[description] AS description_106,b.[name] AS name_106,b.[code] AS code_106 INTO homo_sapiens_vega_58_37c.dbo.TEMP11 FROM homo_sapiens_vega_58_37c.dbo.TEMP10 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.attrib_type AS b ON a.[attrib_type_id_1023]=b.[attrib_type_id];
GO
;
PRINT '34 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP10;
GO
;
PRINT '35 - CREATE INDEX I_11 ON homo_s ...';
CREATE INDEX I_11 ON homo_sapiens_vega_58_37c.dbo.TEMP11(gene_id_1021);
GO
;
PRINT '36 - SELECT a.[gene_id_1021],b.[ ...';
SELECT a.[gene_id_1021],b.[attrib_type_id_1023],b.[code_106],b.[description_106],b.[name_106],b.[value_1023] INTO homo_sapiens_vega_58_37c.dbo.TEMP12 FROM homo_sapiens_vega_58_37c.dbo.gene AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP11 AS b ON a.[gene_id_1021]=b.[gene_id_1021];
GO
;
PRINT '37 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP11;
GO
;
PRINT '38 - EXEC SP_RENAME [dbo.TEMP12] ...';
EXEC SP_RENAME [dbo.TEMP12], [gene__gene_attrib];
GO
;
PRINT '39 - CREATE INDEX I_12 ON homo_s ...';
CREATE INDEX I_12 ON homo_sapiens_vega_58_37c.dbo.gene__gene_attrib(gene_id_1021);
GO
;
PRINT '40 - SELECT a.[gene_id_1021] INT ...';
SELECT a.[gene_id_1021] INTO homo_sapiens_vega_58_37c.dbo.TEMP13 FROM homo_sapiens_vega_58_37c.dbo.gene AS a;
GO
;
PRINT '41 - CREATE INDEX I_13 ON homo_s ...';
CREATE INDEX I_13 ON homo_sapiens_vega_58_37c.dbo.TEMP13(gene_id_1021);
GO
;
PRINT '42 - SELECT a.*,b.[seq_region_id ...';
SELECT a.*,b.[seq_region_id] AS seq_region_id_1057,b.[seq_region_start] AS seq_region_start_1057,b.[name] AS name_1057,b.[splicing_event_id] AS splicing_event_id_1057,b.[type] AS type_1057,b.[seq_region_end] AS seq_region_end_1057,b.[seq_region_strand] AS seq_region_strand_1057 INTO homo_sapiens_vega_58_37c.dbo.TEMP14 FROM homo_sapiens_vega_58_37c.dbo.TEMP13 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.splicing_event AS b ON a.[gene_id_1021]=b.[gene_id];
GO
;
PRINT '43 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP13;
GO
;
PRINT '44 - CREATE INDEX I_14 ON homo_s ...';
CREATE INDEX I_14 ON homo_sapiens_vega_58_37c.dbo.TEMP14(seq_region_id_1057);
GO
;
PRINT '45 - SELECT a.*,b.[name] AS name ...';
SELECT a.*,b.[name] AS name_1053,b.[length] AS length_1053,b.[coord_system_id] AS coord_system_id_1053 INTO homo_sapiens_vega_58_37c.dbo.TEMP15 FROM homo_sapiens_vega_58_37c.dbo.TEMP14 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.seq_region AS b ON a.[seq_region_id_1057]=b.[seq_region_id];
GO
;
PRINT '46 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP14;
GO
;
PRINT '47 - CREATE INDEX I_15 ON homo_s ...';
CREATE INDEX I_15 ON homo_sapiens_vega_58_37c.dbo.TEMP15(coord_system_id_1053);
GO
;
PRINT '48 - SELECT a.*,b.[rank] AS rank ...';
SELECT a.*,b.[rank] AS rank_107,b.[name] AS name_107,b.[attrib] AS attrib_107,b.[species_id] AS species_id_107,b.[version] AS version_107 INTO homo_sapiens_vega_58_37c.dbo.TEMP16 FROM homo_sapiens_vega_58_37c.dbo.TEMP15 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.coord_system AS b ON a.[coord_system_id_1053]=b.[coord_system_id];
GO
;
PRINT '49 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP15;
GO
;
PRINT '50 - CREATE INDEX I_16 ON homo_s ...';
CREATE INDEX I_16 ON homo_sapiens_vega_58_37c.dbo.TEMP16(seq_region_id_1057);
GO
;
PRINT '51 - SELECT a.*,b.[sequence] AS  ...';
SELECT a.*,b.[sequence] AS sequence_1013 INTO homo_sapiens_vega_58_37c.dbo.TEMP17 FROM homo_sapiens_vega_58_37c.dbo.TEMP16 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.dna AS b ON a.[seq_region_id_1057]=b.[seq_region_id];
GO
;
PRINT '52 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP16;
GO
;
PRINT '53 - CREATE INDEX I_17 ON homo_s ...';
CREATE INDEX I_17 ON homo_sapiens_vega_58_37c.dbo.TEMP17(seq_region_id_1057);
GO
;
PRINT '54 - SELECT a.*,b.[sequence] AS  ...';
SELECT a.*,b.[sequence] AS sequence_1015,b.[n_line] AS n_line_1015 INTO homo_sapiens_vega_58_37c.dbo.TEMP18 FROM homo_sapiens_vega_58_37c.dbo.TEMP17 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.dnac AS b ON a.[seq_region_id_1057]=b.[seq_region_id];
GO
;
PRINT '55 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP17;
GO
;
PRINT '56 - CREATE INDEX I_18 ON homo_s ...';
CREATE INDEX I_18 ON homo_sapiens_vega_58_37c.dbo.TEMP18(gene_id_1021);
GO
;
PRINT '57 - SELECT a.[gene_id_1021],b.[ ...';
SELECT a.[gene_id_1021],b.[attrib_107],b.[coord_system_id_1053],b.[length_1053],b.[n_line_1015],b.[name_1053],b.[name_1057],b.[name_107],b.[rank_107],b.[seq_region_end_1057],b.[seq_region_id_1057],b.[seq_region_start_1057],b.[seq_region_strand_1057],b.[sequence_1013],b.[sequence_1015],b.[species_id_107],b.[splicing_event_id_1057],b.[type_1057],b.[version_107] INTO homo_sapiens_vega_58_37c.dbo.TEMP19 FROM homo_sapiens_vega_58_37c.dbo.gene AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP18 AS b ON a.[gene_id_1021]=b.[gene_id_1021];
GO
;
PRINT '58 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP18;
GO
;
PRINT '59 - EXEC SP_RENAME [dbo.TEMP19] ...';
EXEC SP_RENAME [dbo.TEMP19], [gene__splicing_event];
GO
;
PRINT '60 - CREATE INDEX I_19 ON homo_s ...';
CREATE INDEX I_19 ON homo_sapiens_vega_58_37c.dbo.gene__splicing_event(gene_id_1021);
GO
;
PRINT '61 - SELECT a.[gene_id_1021] INT ...';
SELECT a.[gene_id_1021] INTO homo_sapiens_vega_58_37c.dbo.TEMP20 FROM homo_sapiens_vega_58_37c.dbo.gene AS a;
GO
;
PRINT '62 - CREATE INDEX I_20 ON homo_s ...';
CREATE INDEX I_20 ON homo_sapiens_vega_58_37c.dbo.TEMP20(gene_id_1021);
GO
;
PRINT '63 - SELECT a.*,b.[interaction_t ...';
SELECT a.*,b.[interaction_type] AS interaction_type_1069,b.[transcript_id] AS transcript_id_1069 INTO homo_sapiens_vega_58_37c.dbo.TEMP21 FROM homo_sapiens_vega_58_37c.dbo.TEMP20 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.unconventional_transcript_association AS b ON a.[gene_id_1021]=b.[gene_id];
GO
;
PRINT '64 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP20;
GO
;
PRINT '65 - CREATE INDEX I_21 ON homo_s ...';
CREATE INDEX I_21 ON homo_sapiens_vega_58_37c.dbo.TEMP21(gene_id_1021);
GO
;
PRINT '66 - SELECT a.[gene_id_1021],b.[ ...';
SELECT a.[gene_id_1021],b.[interaction_type_1069],b.[transcript_id_1069] INTO homo_sapiens_vega_58_37c.dbo.TEMP22 FROM homo_sapiens_vega_58_37c.dbo.gene AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP21 AS b ON a.[gene_id_1021]=b.[gene_id_1021];
GO
;
PRINT '67 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP21;
GO
;
PRINT '68 - EXEC SP_RENAME [dbo.TEMP22] ...';
EXEC SP_RENAME [dbo.TEMP22], [gene__unconventional_transcript_association];
GO
;
PRINT '69 - CREATE INDEX I_22 ON homo_s ...';
CREATE INDEX I_22 ON homo_sapiens_vega_58_37c.dbo.gene__unconventional_transcript_association(gene_id_1021);
GO
;
PRINT '70 - SELECT a.[module_version_10 ...';
SELECT a.[module_version_102],a.[db_102],a.[program_version_102],a.[program_102],a.[description_1021],a.[canonical_transcript_id_1021],a.[modified_date_1024],a.[length_1053],a.[module_102],a.[name_107],a.[alt_allele_id_101],a.[biotype_1021],a.[seq_region_id_1021],a.[program_file_102],a.[gene_id_1021],a.[logic_name_102],a.[db_file_102],a.[sequence_1015],a.[displayable_103],a.[description_103],a.[version_1024],a.[display_label_103],a.[is_current_1021],a.[sequence_1013],a.[db_version_102],a.[status_1021],a.[seq_region_start_1021],a.[source_1021],a.[gff_feature_102],a.[attrib_107],a.[version_107],a.[seq_region_strand_1021],a.[parameters_102],a.[stable_id_1024],a.[gff_source_102],a.[n_line_1015],a.[web_data_103],a.[seq_region_end_1021],a.[rank_107],a.[species_id_107],a.[display_xref_id_1021],a.[created_date_1024],a.[name_1053],a.[analysis_id_1021],a.[coord_system_id_1053],a.[canonical_annotation_1021],a.[created_102] INTO homo_sapiens_vega_58_37c.dbo.TEMP23 FROM homo_sapiens_vega_58_37c.dbo.gene AS a;
GO
;
PRINT '71 - CREATE INDEX I_23 ON homo_s ...';
CREATE INDEX I_23 ON homo_sapiens_vega_58_37c.dbo.TEMP23(gene_id_1021);
GO
;
PRINT '72 - SELECT a.*,b.[seq_region_id ...';
SELECT a.*,b.[seq_region_id] AS seq_region_id_1062,b.[transcript_id] AS transcript_id_1062,b.[status] AS status_1062,b.[seq_region_start] AS seq_region_start_1062,b.[description] AS description_1062,b.[analysis_id] AS analysis_id_1062,b.[biotype] AS biotype_1062,b.[display_xref_id] AS display_xref_id_1062,b.[is_current] AS is_current_1062,b.[seq_region_end] AS seq_region_end_1062,b.[seq_region_strand] AS seq_region_strand_1062,b.[canonical_translation_id] AS canonical_translation_id_1062 INTO homo_sapiens_vega_58_37c.dbo.TEMP24 FROM homo_sapiens_vega_58_37c.dbo.TEMP23 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.transcript AS b ON a.[gene_id_1021]=b.[gene_id];
GO
;
PRINT '73 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP23;
GO
;
PRINT '74 - CREATE INDEX I_24 ON homo_s ...';
CREATE INDEX I_24 ON homo_sapiens_vega_58_37c.dbo.TEMP24(analysis_id_1062);
GO
;
PRINT '75 - SELECT a.*,b.[db] AS db_102 ...';
SELECT a.*,b.[db] AS db_102_r1,b.[db_version] AS db_version_102_r1,b.[module] AS module_102_r1,b.[gff_source] AS gff_source_102_r1,b.[module_version] AS module_version_102_r1,b.[logic_name] AS logic_name_102_r1,b.[gff_feature] AS gff_feature_102_r1,b.[program_version] AS program_version_102_r1,b.[db_file] AS db_file_102_r1,b.[program_file] AS program_file_102_r1,b.[created] AS created_102_r1,b.[program] AS program_102_r1,b.[parameters] AS parameters_102_r1 INTO homo_sapiens_vega_58_37c.dbo.TEMP25 FROM homo_sapiens_vega_58_37c.dbo.TEMP24 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.analysis AS b ON a.[analysis_id_1062]=b.[analysis_id];
GO
;
PRINT '76 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP24;
GO
;
PRINT '77 - CREATE INDEX I_25 ON homo_s ...';
CREATE INDEX I_25 ON homo_sapiens_vega_58_37c.dbo.TEMP25(analysis_id_1062);
GO
;
PRINT '78 - SELECT a.*,b.[display_label ...';
SELECT a.*,b.[display_label] AS display_label_103_r1,b.[description] AS description_103_r1,b.[displayable] AS displayable_103_r1,b.[web_data] AS web_data_103_r1 INTO homo_sapiens_vega_58_37c.dbo.TEMP26 FROM homo_sapiens_vega_58_37c.dbo.TEMP25 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.analysis_description AS b ON a.[analysis_id_1062]=b.[analysis_id];
GO
;
PRINT '79 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP25;
GO
;
PRINT '80 - CREATE INDEX I_26 ON homo_s ...';
CREATE INDEX I_26 ON homo_sapiens_vega_58_37c.dbo.TEMP26(seq_region_id_1062);
GO
;
PRINT '81 - SELECT a.*,b.[name] AS name ...';
SELECT a.*,b.[name] AS name_1053_r1,b.[length] AS length_1053_r1,b.[coord_system_id] AS coord_system_id_1053_r1 INTO homo_sapiens_vega_58_37c.dbo.TEMP27 FROM homo_sapiens_vega_58_37c.dbo.TEMP26 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.seq_region AS b ON a.[seq_region_id_1062]=b.[seq_region_id];
GO
;
PRINT '82 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP26;
GO
;
PRINT '83 - CREATE INDEX I_27 ON homo_s ...';
CREATE INDEX I_27 ON homo_sapiens_vega_58_37c.dbo.TEMP27(coord_system_id_1053_r1);
GO
;
PRINT '84 - SELECT a.*,b.[rank] AS rank ...';
SELECT a.*,b.[rank] AS rank_107_r1,b.[name] AS name_107_r1,b.[attrib] AS attrib_107_r1,b.[species_id] AS species_id_107_r1,b.[version] AS version_107_r1 INTO homo_sapiens_vega_58_37c.dbo.TEMP28 FROM homo_sapiens_vega_58_37c.dbo.TEMP27 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.coord_system AS b ON a.[coord_system_id_1053_r1]=b.[coord_system_id];
GO
;
PRINT '85 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP27;
GO
;
PRINT '86 - CREATE INDEX I_28 ON homo_s ...';
CREATE INDEX I_28 ON homo_sapiens_vega_58_37c.dbo.TEMP28(seq_region_id_1062);
GO
;
PRINT '87 - SELECT a.*,b.[sequence] AS  ...';
SELECT a.*,b.[sequence] AS sequence_1013_r1 INTO homo_sapiens_vega_58_37c.dbo.TEMP29 FROM homo_sapiens_vega_58_37c.dbo.TEMP28 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.dna AS b ON a.[seq_region_id_1062]=b.[seq_region_id];
GO
;
PRINT '88 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP28;
GO
;
PRINT '89 - CREATE INDEX I_29 ON homo_s ...';
CREATE INDEX I_29 ON homo_sapiens_vega_58_37c.dbo.TEMP29(seq_region_id_1062);
GO
;
PRINT '90 - SELECT a.*,b.[sequence] AS  ...';
SELECT a.*,b.[sequence] AS sequence_1015_r1,b.[n_line] AS n_line_1015_r1 INTO homo_sapiens_vega_58_37c.dbo.TEMP30 FROM homo_sapiens_vega_58_37c.dbo.TEMP29 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.dnac AS b ON a.[seq_region_id_1062]=b.[seq_region_id];
GO
;
PRINT '91 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP29;
GO
;
PRINT '92 - CREATE INDEX I_30 ON homo_s ...';
CREATE INDEX I_30 ON homo_sapiens_vega_58_37c.dbo.TEMP30(transcript_id_1062);
GO
;
PRINT '93 - SELECT a.*,b.[created_date] ...';
SELECT a.*,b.[created_date] AS created_date_1064,b.[stable_id] AS stable_id_1064,b.[modified_date] AS modified_date_1064,b.[version] AS version_1064 INTO homo_sapiens_vega_58_37c.dbo.TEMP31 FROM homo_sapiens_vega_58_37c.dbo.TEMP30 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.transcript_stable_id AS b ON a.[transcript_id_1062]=b.[transcript_id];
GO
;
PRINT '94 - DROP TABLE homo_sapiens_veg ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP30;
GO
;
PRINT '95 - EXEC SP_RENAME [dbo.TEMP31] ...';
EXEC SP_RENAME [dbo.TEMP31], [gene__transcript];
GO
;
PRINT '96 - CREATE INDEX I_31 ON homo_s ...';
CREATE INDEX I_31 ON homo_sapiens_vega_58_37c.dbo.gene__transcript(gene_id_1021);
GO
;
PRINT '97 - CREATE INDEX I_32 ON homo_s ...';
CREATE INDEX I_32 ON homo_sapiens_vega_58_37c.dbo.gene__transcript(transcript_id_1062);
GO
;
PRINT '98 - ALTER TABLE homo_sapiens_ve ...';
ALTER TABLE homo_sapiens_vega_58_37c.dbo.gene ADD gene__transcript_count INTEGER DEFAULT 0;
GO
;
PRINT '99 - UPDATE a SET gene__transcri ...';
UPDATE a SET gene__transcript_count=(SELECT COUNT(1) FROM homo_sapiens_vega_58_37c.dbo.gene__transcript b WHERE a.[gene_id_1021]=b.[gene_id_1021] AND NOT(b.[alt_allele_id_101] IS NULL AND b.[analysis_id_1021] IS NULL AND b.[analysis_id_1062] IS NULL AND b.[attrib_107] IS NULL AND b.[attrib_107_r1] IS NULL AND b.[biotype_1021] IS NULL AND b.[biotype_1062] IS NULL AND b.[canonical_annotation_1021] IS NULL AND b.[canonical_transcript_id_1021] IS NULL AND b.[canonical_translation_id_1062] IS NULL AND b.[coord_system_id_1053] IS NULL AND b.[coord_system_id_1053_r1] IS NULL AND b.[created_102] IS NULL AND b.[created_102_r1] IS NULL AND b.[created_date_1024] IS NULL AND b.[created_date_1064] IS NULL AND b.[db_102] IS NULL AND b.[db_102_r1] IS NULL AND b.[db_file_102] IS NULL AND b.[db_file_102_r1] IS NULL AND b.[db_version_102] IS NULL AND b.[db_version_102_r1] IS NULL AND b.[description_1021] IS NULL AND b.[description_103] IS NULL AND b.[description_103_r1] IS NULL AND b.[description_1062] IS NULL AND b.[display_label_103] IS NULL AND b.[display_label_103_r1] IS NULL AND b.[display_xref_id_1021] IS NULL AND b.[display_xref_id_1062] IS NULL AND b.[displayable_103] IS NULL AND b.[displayable_103_r1] IS NULL AND b.[gff_feature_102] IS NULL AND b.[gff_feature_102_r1] IS NULL AND b.[gff_source_102] IS NULL AND b.[gff_source_102_r1] IS NULL AND b.[is_current_1021] IS NULL AND b.[is_current_1062] IS NULL AND b.[length_1053] IS NULL AND b.[length_1053_r1] IS NULL AND b.[logic_name_102] IS NULL AND b.[logic_name_102_r1] IS NULL AND b.[modified_date_1024] IS NULL AND b.[modified_date_1064] IS NULL AND b.[module_102] IS NULL AND b.[module_102_r1] IS NULL AND b.[module_version_102] IS NULL AND b.[module_version_102_r1] IS NULL AND b.[n_line_1015] IS NULL AND b.[n_line_1015_r1] IS NULL AND b.[name_1053] IS NULL AND b.[name_1053_r1] IS NULL AND b.[name_107] IS NULL AND b.[name_107_r1] IS NULL AND b.[parameters_102] IS NULL AND b.[parameters_102_r1] IS NULL AND b.[program_102] IS NULL AND b.[program_102_r1] IS NULL AND b.[program_file_102] IS NULL AND b.[program_file_102_r1] IS NULL AND b.[program_version_102] IS NULL AND b.[program_version_102_r1] IS NULL AND b.[rank_107] IS NULL AND b.[rank_107_r1] IS NULL AND b.[seq_region_end_1021] IS NULL AND b.[seq_region_end_1062] IS NULL AND b.[seq_region_id_1021] IS NULL AND b.[seq_region_id_1062] IS NULL AND b.[seq_region_start_1021] IS NULL AND b.[seq_region_start_1062] IS NULL AND b.[seq_region_strand_1021] IS NULL AND b.[seq_region_strand_1062] IS NULL AND b.[sequence_1013] IS NULL AND b.[sequence_1013_r1] IS NULL AND b.[sequence_1015] IS NULL AND b.[sequence_1015_r1] IS NULL AND b.[source_1021] IS NULL AND b.[species_id_107] IS NULL AND b.[species_id_107_r1] IS NULL AND b.[stable_id_1024] IS NULL AND b.[stable_id_1064] IS NULL AND b.[status_1021] IS NULL AND b.[status_1062] IS NULL AND b.[transcript_id_1062] IS NULL AND b.[version_1024] IS NULL AND b.[version_1064] IS NULL AND b.[version_107] IS NULL AND b.[version_107_r1] IS NULL AND b.[web_data_103] IS NULL AND b.[web_data_103_r1] IS NULL)) FROM homo_sapiens_vega_58_37c.dbo.gene a;
GO
;
PRINT '100 - CREATE INDEX I_33 ON homo_ ...';
CREATE INDEX I_33 ON homo_sapiens_vega_58_37c.dbo.gene(gene__transcript_count);
GO
;
PRINT '101 - ALTER TABLE homo_sapiens_v ...';
ALTER TABLE homo_sapiens_vega_58_37c.dbo.gene__transcript ADD gene__transcript_count INTEGER DEFAULT 0;
GO
;
PRINT '102 - UPDATE a SET gene__transcr ...';
UPDATE a SET gene__transcript_count=(SELECT MAX(gene__transcript_count) FROM homo_sapiens_vega_58_37c.dbo.gene b WHERE a.[gene_id_1021]=b.[gene_id_1021]) FROM homo_sapiens_vega_58_37c.dbo.gene__transcript a;
GO
;
PRINT '103 - CREATE INDEX I_34 ON homo_ ...';
CREATE INDEX I_34 ON homo_sapiens_vega_58_37c.dbo.gene__transcript(gene__transcript_count);
GO
;
PRINT '104 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062] INTO homo_sapiens_vega_58_37c.dbo.TEMP32 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a;
GO
;
PRINT '105 - CREATE INDEX I_35 ON homo_ ...';
CREATE INDEX I_35 ON homo_sapiens_vega_58_37c.dbo.TEMP32(transcript_id_1062);
GO
;
PRINT '106 - SELECT a.*,b.[rank] AS ran ...';
SELECT a.*,b.[rank] AS rank_1018,b.[exon_id] AS exon_id_1018 INTO homo_sapiens_vega_58_37c.dbo.TEMP33 FROM homo_sapiens_vega_58_37c.dbo.TEMP32 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.exon_transcript AS b ON a.[transcript_id_1062]=b.[transcript_id];
GO
;
PRINT '107 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP32;
GO
;
PRINT '108 - CREATE INDEX I_36 ON homo_ ...';
CREATE INDEX I_36 ON homo_sapiens_vega_58_37c.dbo.TEMP33(exon_id_1018);
GO
;
PRINT '109 - SELECT a.*,b.[seq_region_i ...';
SELECT a.*,b.[seq_region_id] AS seq_region_id_1016,b.[seq_region_start] AS seq_region_start_1016,b.[is_current] AS is_current_1016,b.[end_phase] AS end_phase_1016,b.[phase] AS phase_1016,b.[seq_region_end] AS seq_region_end_1016,b.[seq_region_strand] AS seq_region_strand_1016,b.[is_constitutive] AS is_constitutive_1016 INTO homo_sapiens_vega_58_37c.dbo.TEMP34 FROM homo_sapiens_vega_58_37c.dbo.TEMP33 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.exon AS b ON a.[exon_id_1018]=b.[exon_id];
GO
;
PRINT '110 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP33;
GO
;
PRINT '111 - CREATE INDEX I_37 ON homo_ ...';
CREATE INDEX I_37 ON homo_sapiens_vega_58_37c.dbo.TEMP34(exon_id_1018);
GO
;
PRINT '112 - SELECT a.*,b.[created_date ...';
SELECT a.*,b.[created_date] AS created_date_1017,b.[stable_id] AS stable_id_1017,b.[modified_date] AS modified_date_1017,b.[version] AS version_1017 INTO homo_sapiens_vega_58_37c.dbo.TEMP35 FROM homo_sapiens_vega_58_37c.dbo.TEMP34 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.exon_stable_id AS b ON a.[exon_id_1018]=b.[exon_id];
GO
;
PRINT '113 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP34;
GO
;
PRINT '114 - CREATE INDEX I_38 ON homo_ ...';
CREATE INDEX I_38 ON homo_sapiens_vega_58_37c.dbo.TEMP35(seq_region_id_1016);
GO
;
PRINT '115 - SELECT a.*,b.[name] AS nam ...';
SELECT a.*,b.[name] AS name_1053,b.[length] AS length_1053,b.[coord_system_id] AS coord_system_id_1053 INTO homo_sapiens_vega_58_37c.dbo.TEMP36 FROM homo_sapiens_vega_58_37c.dbo.TEMP35 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.seq_region AS b ON a.[seq_region_id_1016]=b.[seq_region_id];
GO
;
PRINT '116 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP35;
GO
;
PRINT '117 - CREATE INDEX I_39 ON homo_ ...';
CREATE INDEX I_39 ON homo_sapiens_vega_58_37c.dbo.TEMP36(coord_system_id_1053);
GO
;
PRINT '118 - SELECT a.*,b.[rank] AS ran ...';
SELECT a.*,b.[rank] AS rank_107,b.[name] AS name_107,b.[attrib] AS attrib_107,b.[species_id] AS species_id_107,b.[version] AS version_107 INTO homo_sapiens_vega_58_37c.dbo.TEMP37 FROM homo_sapiens_vega_58_37c.dbo.TEMP36 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.coord_system AS b ON a.[coord_system_id_1053]=b.[coord_system_id];
GO
;
PRINT '119 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP36;
GO
;
PRINT '120 - CREATE INDEX I_40 ON homo_ ...';
CREATE INDEX I_40 ON homo_sapiens_vega_58_37c.dbo.TEMP37(seq_region_id_1016);
GO
;
PRINT '121 - SELECT a.*,b.[sequence] AS ...';
SELECT a.*,b.[sequence] AS sequence_1013 INTO homo_sapiens_vega_58_37c.dbo.TEMP38 FROM homo_sapiens_vega_58_37c.dbo.TEMP37 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.dna AS b ON a.[seq_region_id_1016]=b.[seq_region_id];
GO
;
PRINT '122 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP37;
GO
;
PRINT '123 - CREATE INDEX I_41 ON homo_ ...';
CREATE INDEX I_41 ON homo_sapiens_vega_58_37c.dbo.TEMP38(seq_region_id_1016);
GO
;
PRINT '124 - SELECT a.*,b.[sequence] AS ...';
SELECT a.*,b.[sequence] AS sequence_1015,b.[n_line] AS n_line_1015 INTO homo_sapiens_vega_58_37c.dbo.TEMP39 FROM homo_sapiens_vega_58_37c.dbo.TEMP38 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.dnac AS b ON a.[seq_region_id_1016]=b.[seq_region_id];
GO
;
PRINT '125 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP38;
GO
;
PRINT '126 - CREATE INDEX I_42 ON homo_ ...';
CREATE INDEX I_42 ON homo_sapiens_vega_58_37c.dbo.TEMP39(transcript_id_1062);
GO
;
PRINT '127 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062],b.[attrib_107],b.[coord_system_id_1053],b.[created_date_1017],b.[end_phase_1016],b.[exon_id_1018],b.[is_constitutive_1016],b.[is_current_1016],b.[length_1053],b.[modified_date_1017],b.[n_line_1015],b.[name_1053],b.[name_107],b.[phase_1016],b.[rank_1018],b.[rank_107],b.[seq_region_end_1016],b.[seq_region_id_1016],b.[seq_region_start_1016],b.[seq_region_strand_1016],b.[sequence_1013],b.[sequence_1015],b.[species_id_107],b.[stable_id_1017],b.[version_1017],b.[version_107] INTO homo_sapiens_vega_58_37c.dbo.TEMP40 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP39 AS b ON a.[transcript_id_1062]=b.[transcript_id_1062];
GO
;
PRINT '128 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP39;
GO
;
PRINT '129 - EXEC SP_RENAME [dbo.TEMP40 ...';
EXEC SP_RENAME [dbo.TEMP40], [transcript__exon_transcript];
GO
;
PRINT '130 - CREATE INDEX I_43 ON homo_ ...';
CREATE INDEX I_43 ON homo_sapiens_vega_58_37c.dbo.transcript__exon_transcript(transcript_id_1062);
GO
;
PRINT '131 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062] INTO homo_sapiens_vega_58_37c.dbo.TEMP41 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a;
GO
;
PRINT '132 - CREATE INDEX I_44 ON homo_ ...';
CREATE INDEX I_44 ON homo_sapiens_vega_58_37c.dbo.TEMP41(transcript_id_1062);
GO
;
PRINT '133 - SELECT a.*,b.[splicing_eve ...';
SELECT a.*,b.[splicing_event_feature_id] AS splicing_event_feature_id_1058,b.[start2] AS start2_1058,b.[end2] AS end2_1058,b.[feature_order] AS feature_order_1058,b.[exon_id] AS exon_id_1058,b.[transcript_association] AS transcript_association_1058,b.[splicing_event_id] AS splicing_event_id_1058,b.[type] AS type_1058 INTO homo_sapiens_vega_58_37c.dbo.TEMP42 FROM homo_sapiens_vega_58_37c.dbo.TEMP41 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.splicing_event_feature AS b ON a.[transcript_id_1062]=b.[transcript_id];
GO
;
PRINT '134 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP41;
GO
;
PRINT '135 - CREATE INDEX I_45 ON homo_ ...';
CREATE INDEX I_45 ON homo_sapiens_vega_58_37c.dbo.TEMP42(exon_id_1058);
GO
;
PRINT '136 - SELECT a.*,b.[seq_region_i ...';
SELECT a.*,b.[seq_region_id] AS seq_region_id_1016,b.[seq_region_start] AS seq_region_start_1016,b.[is_current] AS is_current_1016,b.[end_phase] AS end_phase_1016,b.[phase] AS phase_1016,b.[seq_region_end] AS seq_region_end_1016,b.[seq_region_strand] AS seq_region_strand_1016,b.[is_constitutive] AS is_constitutive_1016 INTO homo_sapiens_vega_58_37c.dbo.TEMP43 FROM homo_sapiens_vega_58_37c.dbo.TEMP42 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.exon AS b ON a.[exon_id_1058]=b.[exon_id];
GO
;
PRINT '137 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP42;
GO
;
PRINT '138 - CREATE INDEX I_46 ON homo_ ...';
CREATE INDEX I_46 ON homo_sapiens_vega_58_37c.dbo.TEMP43(exon_id_1058);
GO
;
PRINT '139 - SELECT a.*,b.[created_date ...';
SELECT a.*,b.[created_date] AS created_date_1017,b.[stable_id] AS stable_id_1017,b.[modified_date] AS modified_date_1017,b.[version] AS version_1017 INTO homo_sapiens_vega_58_37c.dbo.TEMP44 FROM homo_sapiens_vega_58_37c.dbo.TEMP43 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.exon_stable_id AS b ON a.[exon_id_1058]=b.[exon_id];
GO
;
PRINT '140 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP43;
GO
;
PRINT '141 - CREATE INDEX I_47 ON homo_ ...';
CREATE INDEX I_47 ON homo_sapiens_vega_58_37c.dbo.TEMP44(seq_region_id_1016);
GO
;
PRINT '142 - SELECT a.*,b.[name] AS nam ...';
SELECT a.*,b.[name] AS name_1053,b.[length] AS length_1053,b.[coord_system_id] AS coord_system_id_1053 INTO homo_sapiens_vega_58_37c.dbo.TEMP45 FROM homo_sapiens_vega_58_37c.dbo.TEMP44 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.seq_region AS b ON a.[seq_region_id_1016]=b.[seq_region_id];
GO
;
PRINT '143 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP44;
GO
;
PRINT '144 - CREATE INDEX I_48 ON homo_ ...';
CREATE INDEX I_48 ON homo_sapiens_vega_58_37c.dbo.TEMP45(coord_system_id_1053);
GO
;
PRINT '145 - SELECT a.*,b.[rank] AS ran ...';
SELECT a.*,b.[rank] AS rank_107,b.[name] AS name_107,b.[attrib] AS attrib_107,b.[species_id] AS species_id_107,b.[version] AS version_107 INTO homo_sapiens_vega_58_37c.dbo.TEMP46 FROM homo_sapiens_vega_58_37c.dbo.TEMP45 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.coord_system AS b ON a.[coord_system_id_1053]=b.[coord_system_id];
GO
;
PRINT '146 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP45;
GO
;
PRINT '147 - CREATE INDEX I_49 ON homo_ ...';
CREATE INDEX I_49 ON homo_sapiens_vega_58_37c.dbo.TEMP46(seq_region_id_1016);
GO
;
PRINT '148 - SELECT a.*,b.[sequence] AS ...';
SELECT a.*,b.[sequence] AS sequence_1013 INTO homo_sapiens_vega_58_37c.dbo.TEMP47 FROM homo_sapiens_vega_58_37c.dbo.TEMP46 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.dna AS b ON a.[seq_region_id_1016]=b.[seq_region_id];
GO
;
PRINT '149 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP46;
GO
;
PRINT '150 - CREATE INDEX I_50 ON homo_ ...';
CREATE INDEX I_50 ON homo_sapiens_vega_58_37c.dbo.TEMP47(seq_region_id_1016);
GO
;
PRINT '151 - SELECT a.*,b.[sequence] AS ...';
SELECT a.*,b.[sequence] AS sequence_1015,b.[n_line] AS n_line_1015 INTO homo_sapiens_vega_58_37c.dbo.TEMP48 FROM homo_sapiens_vega_58_37c.dbo.TEMP47 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.dnac AS b ON a.[seq_region_id_1016]=b.[seq_region_id];
GO
;
PRINT '152 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP47;
GO
;
PRINT '153 - CREATE INDEX I_51 ON homo_ ...';
CREATE INDEX I_51 ON homo_sapiens_vega_58_37c.dbo.TEMP48(splicing_event_id_1058);
GO
;
PRINT '154 - SELECT a.*,b.[seq_region_i ...';
SELECT a.*,b.[seq_region_id] AS seq_region_id_1057,b.[seq_region_start] AS seq_region_start_1057,b.[name] AS name_1057,b.[gene_id] AS gene_id_1057,b.[type] AS type_1057,b.[seq_region_end] AS seq_region_end_1057,b.[seq_region_strand] AS seq_region_strand_1057 INTO homo_sapiens_vega_58_37c.dbo.TEMP49 FROM homo_sapiens_vega_58_37c.dbo.TEMP48 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.splicing_event AS b ON a.[splicing_event_id_1058]=b.[splicing_event_id];
GO
;
PRINT '155 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP48;
GO
;
PRINT '156 - CREATE INDEX I_52 ON homo_ ...';
CREATE INDEX I_52 ON homo_sapiens_vega_58_37c.dbo.TEMP49(seq_region_id_1057);
GO
;
PRINT '157 - SELECT a.*,b.[name] AS nam ...';
SELECT a.*,b.[name] AS name_1153,b.[length] AS length_1153,b.[coord_system_id] AS coord_system_id_1153 INTO homo_sapiens_vega_58_37c.dbo.TEMP50 FROM homo_sapiens_vega_58_37c.dbo.TEMP49 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.seq_region AS b ON a.[seq_region_id_1057]=b.[seq_region_id];
GO
;
PRINT '158 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP49;
GO
;
PRINT '159 - CREATE INDEX I_53 ON homo_ ...';
CREATE INDEX I_53 ON homo_sapiens_vega_58_37c.dbo.TEMP50(coord_system_id_1153);
GO
;
PRINT '160 - SELECT a.*,b.[rank] AS ran ...';
SELECT a.*,b.[rank] AS rank_117,b.[name] AS name_117,b.[attrib] AS attrib_117,b.[species_id] AS species_id_117,b.[version] AS version_117 INTO homo_sapiens_vega_58_37c.dbo.TEMP51 FROM homo_sapiens_vega_58_37c.dbo.TEMP50 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.coord_system AS b ON a.[coord_system_id_1153]=b.[coord_system_id];
GO
;
PRINT '161 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP50;
GO
;
PRINT '162 - CREATE INDEX I_54 ON homo_ ...';
CREATE INDEX I_54 ON homo_sapiens_vega_58_37c.dbo.TEMP51(seq_region_id_1057);
GO
;
PRINT '163 - SELECT a.*,b.[sequence] AS ...';
SELECT a.*,b.[sequence] AS sequence_1113 INTO homo_sapiens_vega_58_37c.dbo.TEMP52 FROM homo_sapiens_vega_58_37c.dbo.TEMP51 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.dna AS b ON a.[seq_region_id_1057]=b.[seq_region_id];
GO
;
PRINT '164 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP51;
GO
;
PRINT '165 - CREATE INDEX I_55 ON homo_ ...';
CREATE INDEX I_55 ON homo_sapiens_vega_58_37c.dbo.TEMP52(seq_region_id_1057);
GO
;
PRINT '166 - SELECT a.*,b.[sequence] AS ...';
SELECT a.*,b.[sequence] AS sequence_1115,b.[n_line] AS n_line_1115 INTO homo_sapiens_vega_58_37c.dbo.TEMP53 FROM homo_sapiens_vega_58_37c.dbo.TEMP52 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.dnac AS b ON a.[seq_region_id_1057]=b.[seq_region_id];
GO
;
PRINT '167 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP52;
GO
;
PRINT '168 - CREATE INDEX I_56 ON homo_ ...';
CREATE INDEX I_56 ON homo_sapiens_vega_58_37c.dbo.TEMP53(transcript_id_1062);
GO
;
PRINT '169 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062],b.[attrib_107],b.[attrib_117],b.[coord_system_id_1053],b.[coord_system_id_1153],b.[created_date_1017],b.[end2_1058],b.[end_phase_1016],b.[exon_id_1058],b.[feature_order_1058],b.[gene_id_1057],b.[is_constitutive_1016],b.[is_current_1016],b.[length_1053],b.[length_1153],b.[modified_date_1017],b.[n_line_1015],b.[n_line_1115],b.[name_1053],b.[name_1057],b.[name_107],b.[name_1153],b.[name_117],b.[phase_1016],b.[rank_107],b.[rank_117],b.[seq_region_end_1016],b.[seq_region_end_1057],b.[seq_region_id_1016],b.[seq_region_id_1057],b.[seq_region_start_1016],b.[seq_region_start_1057],b.[seq_region_strand_1016],b.[seq_region_strand_1057],b.[sequence_1013],b.[sequence_1015],b.[sequence_1113],b.[sequence_1115],b.[species_id_107],b.[species_id_117],b.[splicing_event_feature_id_1058],b.[splicing_event_id_1058],b.[stable_id_1017],b.[start2_1058],b.[transcript_association_1058],b.[type_1057],b.[type_1058],b.[version_1017],b.[version_107],b.[version_117] INTO homo_sapiens_vega_58_37c.dbo.TEMP54 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP53 AS b ON a.[transcript_id_1062]=b.[transcript_id_1062];
GO
;
PRINT '170 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP53;
GO
;
PRINT '171 - EXEC SP_RENAME [dbo.TEMP54 ...';
EXEC SP_RENAME [dbo.TEMP54], [transcript__splicing_event_feature];
GO
;
PRINT '172 - CREATE INDEX I_57 ON homo_ ...';
CREATE INDEX I_57 ON homo_sapiens_vega_58_37c.dbo.transcript__splicing_event_feature(transcript_id_1062);
GO
;
PRINT '173 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062] INTO homo_sapiens_vega_58_37c.dbo.TEMP55 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a;
GO
;
PRINT '174 - CREATE INDEX I_58 ON homo_ ...';
CREATE INDEX I_58 ON homo_sapiens_vega_58_37c.dbo.TEMP55(transcript_id_1062);
GO
;
PRINT '175 - SELECT a.*,b.[value] AS va ...';
SELECT a.*,b.[value] AS value_1063,b.[attrib_type_id] AS attrib_type_id_1063 INTO homo_sapiens_vega_58_37c.dbo.TEMP56 FROM homo_sapiens_vega_58_37c.dbo.TEMP55 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.transcript_attrib AS b ON a.[transcript_id_1062]=b.[transcript_id];
GO
;
PRINT '176 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP55;
GO
;
PRINT '177 - CREATE INDEX I_59 ON homo_ ...';
CREATE INDEX I_59 ON homo_sapiens_vega_58_37c.dbo.TEMP56(attrib_type_id_1063);
GO
;
PRINT '178 - SELECT a.*,b.[description] ...';
SELECT a.*,b.[description] AS description_106,b.[name] AS name_106,b.[code] AS code_106 INTO homo_sapiens_vega_58_37c.dbo.TEMP57 FROM homo_sapiens_vega_58_37c.dbo.TEMP56 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.attrib_type AS b ON a.[attrib_type_id_1063]=b.[attrib_type_id];
GO
;
PRINT '179 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP56;
GO
;
PRINT '180 - CREATE INDEX I_60 ON homo_ ...';
CREATE INDEX I_60 ON homo_sapiens_vega_58_37c.dbo.TEMP57(transcript_id_1062);
GO
;
PRINT '181 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062],b.[attrib_type_id_1063],b.[code_106],b.[description_106],b.[name_106],b.[value_1063] INTO homo_sapiens_vega_58_37c.dbo.TEMP58 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP57 AS b ON a.[transcript_id_1062]=b.[transcript_id_1062];
GO
;
PRINT '182 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP57;
GO
;
PRINT '183 - EXEC SP_RENAME [dbo.TEMP58 ...';
EXEC SP_RENAME [dbo.TEMP58], [transcript__transcript_attrib];
GO
;
PRINT '184 - CREATE INDEX I_61 ON homo_ ...';
CREATE INDEX I_61 ON homo_sapiens_vega_58_37c.dbo.transcript__transcript_attrib(transcript_id_1062);
GO
;
PRINT '185 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062] INTO homo_sapiens_vega_58_37c.dbo.TEMP59 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a;
GO
;
PRINT '186 - CREATE INDEX I_62 ON homo_ ...';
CREATE INDEX I_62 ON homo_sapiens_vega_58_37c.dbo.TEMP59(transcript_id_1062);
GO
;
PRINT '187 - SELECT a.*,b.[feature_type ...';
SELECT a.*,b.[feature_type] AS feature_type_1065,b.[feature_id] AS feature_id_1065 INTO homo_sapiens_vega_58_37c.dbo.TEMP60 FROM homo_sapiens_vega_58_37c.dbo.TEMP59 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.transcript_supporting_feature AS b ON a.[transcript_id_1062]=b.[transcript_id];
GO
;
PRINT '188 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP59;
GO
;
PRINT '189 - CREATE INDEX I_63 ON homo_ ...';
CREATE INDEX I_63 ON homo_sapiens_vega_58_37c.dbo.TEMP60(transcript_id_1062);
GO
;
PRINT '190 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062],b.[feature_id_1065],b.[feature_type_1065] INTO homo_sapiens_vega_58_37c.dbo.TEMP61 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP60 AS b ON a.[transcript_id_1062]=b.[transcript_id_1062];
GO
;
PRINT '191 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP60;
GO
;
PRINT '192 - EXEC SP_RENAME [dbo.TEMP61 ...';
EXEC SP_RENAME [dbo.TEMP61], [transcript__transcript_supporting_feature];
GO
;
PRINT '193 - CREATE INDEX I_64 ON homo_ ...';
CREATE INDEX I_64 ON homo_sapiens_vega_58_37c.dbo.transcript__transcript_supporting_feature(transcript_id_1062);
GO
;
PRINT '194 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062] INTO homo_sapiens_vega_58_37c.dbo.TEMP62 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a;
GO
;
PRINT '195 - CREATE INDEX I_65 ON homo_ ...';
CREATE INDEX I_65 ON homo_sapiens_vega_58_37c.dbo.TEMP62(transcript_id_1062);
GO
;
PRINT '196 - SELECT a.*,b.[interaction_ ...';
SELECT a.*,b.[interaction_type] AS interaction_type_1069,b.[gene_id] AS gene_id_1069 INTO homo_sapiens_vega_58_37c.dbo.TEMP63 FROM homo_sapiens_vega_58_37c.dbo.TEMP62 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.unconventional_transcript_association AS b ON a.[transcript_id_1062]=b.[transcript_id];
GO
;
PRINT '197 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP62;
GO
;
PRINT '198 - CREATE INDEX I_66 ON homo_ ...';
CREATE INDEX I_66 ON homo_sapiens_vega_58_37c.dbo.TEMP63(transcript_id_1062);
GO
;
PRINT '199 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062],b.[gene_id_1069],b.[interaction_type_1069] INTO homo_sapiens_vega_58_37c.dbo.TEMP64 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP63 AS b ON a.[transcript_id_1062]=b.[transcript_id_1062];
GO
;
PRINT '200 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP63;
GO
;
PRINT '201 - EXEC SP_RENAME [dbo.TEMP64 ...';
EXEC SP_RENAME [dbo.TEMP64], [transcript__unconventional_transcript_association];
GO
;
PRINT '202 - CREATE INDEX I_67 ON homo_ ...';
CREATE INDEX I_67 ON homo_sapiens_vega_58_37c.dbo.transcript__unconventional_transcript_association(transcript_id_1062);
GO
;
PRINT '203 - SELECT a.[transcript_id_10 ...';
SELECT a.[transcript_id_1062],a.[module_version_102],a.[species_id_107_r1],a.[program_version_102],a.[description_1021],a.[is_current_1062],a.[module_version_102_r1],a.[canonical_transcript_id_1021],a.[seq_region_id_1062],a.[program_version_102_r1],a.[length_1053],a.[name_107],a.[sequence_1015_r1],a.[coord_system_id_1053_r1],a.[displayable_103_r1],a.[version_107_r1],a.[alt_allele_id_101],a.[seq_region_id_1021],a.[db_file_102_r1],a.[gene_id_1021],a.[name_1053_r1],a.[logic_name_102],a.[parameters_102_r1],a.[db_102_r1],a.[version_1024],a.[display_label_103],a.[is_current_1021],a.[sequence_1013],a.[analysis_id_1062],a.[status_1021],a.[attrib_107_r1],a.[seq_region_start_1021],a.[source_1021],a.[gff_feature_102],a.[n_line_1015_r1],a.[attrib_107],a.[version_107],a.[seq_region_end_1062],a.[parameters_102],a.[stable_id_1024],a.[gff_source_102],a.[name_107_r1],a.[web_data_103],a.[seq_region_end_1021],a.[display_xref_id_1021],a.[name_1053],a.[analysis_id_1021],a.[status_1062],a.[seq_region_start_1062],a.[coord_system_id_1053],a.[canonical_annotation_1021],a.[created_102],a.[display_xref_id_1062],a.[gene__transcript_count],a.[web_data_103_r1],a.[program_102_r1],a.[db_102],a.[canonical_translation_id_1062],a.[program_102],a.[gff_feature_102_r1],a.[sequence_1013_r1],a.[created_date_1064],a.[modified_date_1024],a.[modified_date_1064],a.[module_102],a.[biotype_1021],a.[program_file_102],a.[sequence_1015],a.[db_file_102],a.[db_version_102_r1],a.[description_103],a.[displayable_103],a.[biotype_1062],a.[stable_id_1064],a.[rank_107_r1],a.[db_version_102],a.[seq_region_strand_1062],a.[gff_source_102_r1],a.[version_1064],a.[created_102_r1],a.[logic_name_102_r1],a.[seq_region_strand_1021],a.[description_103_r1],a.[module_102_r1],a.[n_line_1015],a.[rank_107],a.[species_id_107],a.[program_file_102_r1],a.[created_date_1024],a.[display_label_103_r1],a.[length_1053_r1],a.[description_1062] INTO homo_sapiens_vega_58_37c.dbo.TEMP65 FROM homo_sapiens_vega_58_37c.dbo.gene__transcript AS a;
GO
;
PRINT '204 - CREATE INDEX I_68 ON homo_ ...';
CREATE INDEX I_68 ON homo_sapiens_vega_58_37c.dbo.TEMP65(transcript_id_1062);
GO
;
PRINT '205 - SELECT a.*,b.[seq_start] A ...';
SELECT a.*,b.[seq_start] AS seq_start_1066,b.[start_exon_id] AS start_exon_id_1066,b.[end_exon_id] AS end_exon_id_1066,b.[seq_end] AS seq_end_1066,b.[translation_id] AS translation_id_1066 INTO homo_sapiens_vega_58_37c.dbo.TEMP66 FROM homo_sapiens_vega_58_37c.dbo.TEMP65 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.translation AS b ON a.[transcript_id_1062]=b.[transcript_id];
GO
;
PRINT '206 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP65;
GO
;
PRINT '207 - CREATE INDEX I_69 ON homo_ ...';
CREATE INDEX I_69 ON homo_sapiens_vega_58_37c.dbo.TEMP66(translation_id_1066);
GO
;
PRINT '208 - SELECT a.*,b.[created_date ...';
SELECT a.*,b.[created_date] AS created_date_1068,b.[stable_id] AS stable_id_1068,b.[modified_date] AS modified_date_1068,b.[version] AS version_1068 INTO homo_sapiens_vega_58_37c.dbo.TEMP67 FROM homo_sapiens_vega_58_37c.dbo.TEMP66 AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.translation_stable_id AS b ON a.[translation_id_1066]=b.[translation_id];
GO
;
PRINT '209 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP66;
GO
;
PRINT '210 - EXEC SP_RENAME [dbo.TEMP67 ...';
EXEC SP_RENAME [dbo.TEMP67], [transcript__translation];
GO
;
PRINT '211 - CREATE INDEX I_70 ON homo_ ...';
CREATE INDEX I_70 ON homo_sapiens_vega_58_37c.dbo.transcript__translation(transcript_id_1062);
GO
;
PRINT '212 - CREATE INDEX I_71 ON homo_ ...';
CREATE INDEX I_71 ON homo_sapiens_vega_58_37c.dbo.transcript__translation(translation_id_1066);
GO
;
PRINT '213 - ALTER TABLE homo_sapiens_v ...';
ALTER TABLE homo_sapiens_vega_58_37c.dbo.gene__transcript ADD transcript__translation_count INTEGER DEFAULT 0;
GO
;
PRINT '214 - UPDATE a SET transcript__t ...';
UPDATE a SET transcript__translation_count=(SELECT COUNT(1) FROM homo_sapiens_vega_58_37c.dbo.transcript__translation b WHERE a.[transcript_id_1062]=b.[transcript_id_1062] AND NOT(b.[alt_allele_id_101] IS NULL AND b.[analysis_id_1021] IS NULL AND b.[analysis_id_1062] IS NULL AND b.[attrib_107] IS NULL AND b.[attrib_107_r1] IS NULL AND b.[biotype_1021] IS NULL AND b.[biotype_1062] IS NULL AND b.[canonical_annotation_1021] IS NULL AND b.[canonical_transcript_id_1021] IS NULL AND b.[canonical_translation_id_1062] IS NULL AND b.[coord_system_id_1053] IS NULL AND b.[coord_system_id_1053_r1] IS NULL AND b.[created_102] IS NULL AND b.[created_102_r1] IS NULL AND b.[created_date_1024] IS NULL AND b.[created_date_1064] IS NULL AND b.[created_date_1068] IS NULL AND b.[db_102] IS NULL AND b.[db_102_r1] IS NULL AND b.[db_file_102] IS NULL AND b.[db_file_102_r1] IS NULL AND b.[db_version_102] IS NULL AND b.[db_version_102_r1] IS NULL AND b.[description_1021] IS NULL AND b.[description_103] IS NULL AND b.[description_103_r1] IS NULL AND b.[description_1062] IS NULL AND b.[display_label_103] IS NULL AND b.[display_label_103_r1] IS NULL AND b.[display_xref_id_1021] IS NULL AND b.[display_xref_id_1062] IS NULL AND b.[displayable_103] IS NULL AND b.[displayable_103_r1] IS NULL AND b.[end_exon_id_1066] IS NULL AND b.[gene_id_1021] IS NULL AND b.[gff_feature_102] IS NULL AND b.[gff_feature_102_r1] IS NULL AND b.[gff_source_102] IS NULL AND b.[gff_source_102_r1] IS NULL AND b.[is_current_1021] IS NULL AND b.[is_current_1062] IS NULL AND b.[length_1053] IS NULL AND b.[length_1053_r1] IS NULL AND b.[logic_name_102] IS NULL AND b.[logic_name_102_r1] IS NULL AND b.[modified_date_1024] IS NULL AND b.[modified_date_1064] IS NULL AND b.[modified_date_1068] IS NULL AND b.[module_102] IS NULL AND b.[module_102_r1] IS NULL AND b.[module_version_102] IS NULL AND b.[module_version_102_r1] IS NULL AND b.[n_line_1015] IS NULL AND b.[n_line_1015_r1] IS NULL AND b.[name_1053] IS NULL AND b.[name_1053_r1] IS NULL AND b.[name_107] IS NULL AND b.[name_107_r1] IS NULL AND b.[parameters_102] IS NULL AND b.[parameters_102_r1] IS NULL AND b.[program_102] IS NULL AND b.[program_102_r1] IS NULL AND b.[program_file_102] IS NULL AND b.[program_file_102_r1] IS NULL AND b.[program_version_102] IS NULL AND b.[program_version_102_r1] IS NULL AND b.[rank_107] IS NULL AND b.[rank_107_r1] IS NULL AND b.[seq_end_1066] IS NULL AND b.[seq_region_end_1021] IS NULL AND b.[seq_region_end_1062] IS NULL AND b.[seq_region_id_1021] IS NULL AND b.[seq_region_id_1062] IS NULL AND b.[seq_region_start_1021] IS NULL AND b.[seq_region_start_1062] IS NULL AND b.[seq_region_strand_1021] IS NULL AND b.[seq_region_strand_1062] IS NULL AND b.[seq_start_1066] IS NULL AND b.[sequence_1013] IS NULL AND b.[sequence_1013_r1] IS NULL AND b.[sequence_1015] IS NULL AND b.[sequence_1015_r1] IS NULL AND b.[source_1021] IS NULL AND b.[species_id_107] IS NULL AND b.[species_id_107_r1] IS NULL AND b.[stable_id_1024] IS NULL AND b.[stable_id_1064] IS NULL AND b.[stable_id_1068] IS NULL AND b.[start_exon_id_1066] IS NULL AND b.[status_1021] IS NULL AND b.[status_1062] IS NULL AND b.[translation_id_1066] IS NULL AND b.[version_1024] IS NULL AND b.[version_1064] IS NULL AND b.[version_1068] IS NULL AND b.[version_107] IS NULL AND b.[version_107_r1] IS NULL AND b.[web_data_103] IS NULL AND b.[web_data_103_r1] IS NULL)) FROM homo_sapiens_vega_58_37c.dbo.gene__transcript a;
GO
;
PRINT '215 - CREATE INDEX I_72 ON homo_ ...';
CREATE INDEX I_72 ON homo_sapiens_vega_58_37c.dbo.gene__transcript(transcript__translation_count);
GO
;
PRINT '216 - ALTER TABLE homo_sapiens_v ...';
ALTER TABLE homo_sapiens_vega_58_37c.dbo.transcript__translation ADD transcript__translation_count INTEGER DEFAULT 0;
GO
;
PRINT '217 - UPDATE a SET transcript__t ...';
UPDATE a SET transcript__translation_count=(SELECT MAX(transcript__translation_count) FROM homo_sapiens_vega_58_37c.dbo.gene__transcript b WHERE a.[transcript_id_1062]=b.[transcript_id_1062]) FROM homo_sapiens_vega_58_37c.dbo.transcript__translation a;
GO
;
PRINT '218 - CREATE INDEX I_73 ON homo_ ...';
CREATE INDEX I_73 ON homo_sapiens_vega_58_37c.dbo.transcript__translation(transcript__translation_count);
GO
;
PRINT '219 - SELECT a.[translation_id_1 ...';
SELECT a.[translation_id_1066] INTO homo_sapiens_vega_58_37c.dbo.TEMP68 FROM homo_sapiens_vega_58_37c.dbo.transcript__translation AS a;
GO
;
PRINT '220 - CREATE INDEX I_74 ON homo_ ...';
CREATE INDEX I_74 ON homo_sapiens_vega_58_37c.dbo.TEMP68(translation_id_1066);
GO
;
PRINT '221 - SELECT a.*,b.[seq_start] A ...';
SELECT a.*,b.[seq_start] AS seq_start_1047,b.[hit_end] AS hit_end_1047,b.[perc_ident] AS perc_ident_1047,b.[analysis_id] AS analysis_id_1047,b.[hit_start] AS hit_start_1047,b.[hit_name] AS hit_name_1047,b.[score] AS score_1047,b.[protein_feature_id] AS protein_feature_id_1047,b.[seq_end] AS seq_end_1047,b.[evalue] AS evalue_1047,b.[external_data] AS external_data_1047 INTO homo_sapiens_vega_58_37c.dbo.TEMP69 FROM homo_sapiens_vega_58_37c.dbo.TEMP68 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.protein_feature AS b ON a.[translation_id_1066]=b.[translation_id];
GO
;
PRINT '222 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP68;
GO
;
PRINT '223 - CREATE INDEX I_75 ON homo_ ...';
CREATE INDEX I_75 ON homo_sapiens_vega_58_37c.dbo.TEMP69(analysis_id_1047);
GO
;
PRINT '224 - SELECT a.*,b.[db] AS db_10 ...';
SELECT a.*,b.[db] AS db_102,b.[db_version] AS db_version_102,b.[module] AS module_102,b.[gff_source] AS gff_source_102,b.[module_version] AS module_version_102,b.[logic_name] AS logic_name_102,b.[gff_feature] AS gff_feature_102,b.[program_version] AS program_version_102,b.[db_file] AS db_file_102,b.[program_file] AS program_file_102,b.[created] AS created_102,b.[program] AS program_102,b.[parameters] AS parameters_102 INTO homo_sapiens_vega_58_37c.dbo.TEMP70 FROM homo_sapiens_vega_58_37c.dbo.TEMP69 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.analysis AS b ON a.[analysis_id_1047]=b.[analysis_id];
GO
;
PRINT '225 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP69;
GO
;
PRINT '226 - CREATE INDEX I_76 ON homo_ ...';
CREATE INDEX I_76 ON homo_sapiens_vega_58_37c.dbo.TEMP70(analysis_id_1047);
GO
;
PRINT '227 - SELECT a.*,b.[display_labe ...';
SELECT a.*,b.[display_label] AS display_label_103,b.[description] AS description_103,b.[displayable] AS displayable_103,b.[web_data] AS web_data_103 INTO homo_sapiens_vega_58_37c.dbo.TEMP71 FROM homo_sapiens_vega_58_37c.dbo.TEMP70 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.analysis_description AS b ON a.[analysis_id_1047]=b.[analysis_id];
GO
;
PRINT '228 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP70;
GO
;
PRINT '229 - CREATE INDEX I_77 ON homo_ ...';
CREATE INDEX I_77 ON homo_sapiens_vega_58_37c.dbo.TEMP71(translation_id_1066);
GO
;
PRINT '230 - SELECT a.[translation_id_1 ...';
SELECT a.[translation_id_1066],b.[analysis_id_1047],b.[created_102],b.[db_102],b.[db_file_102],b.[db_version_102],b.[description_103],b.[display_label_103],b.[displayable_103],b.[evalue_1047],b.[external_data_1047],b.[gff_feature_102],b.[gff_source_102],b.[hit_end_1047],b.[hit_name_1047],b.[hit_start_1047],b.[logic_name_102],b.[module_102],b.[module_version_102],b.[parameters_102],b.[perc_ident_1047],b.[program_102],b.[program_file_102],b.[program_version_102],b.[protein_feature_id_1047],b.[score_1047],b.[seq_end_1047],b.[seq_start_1047],b.[web_data_103] INTO homo_sapiens_vega_58_37c.dbo.TEMP72 FROM homo_sapiens_vega_58_37c.dbo.transcript__translation AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP71 AS b ON a.[translation_id_1066]=b.[translation_id_1066];
GO
;
PRINT '231 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP71;
GO
;
PRINT '232 - EXEC SP_RENAME [dbo.TEMP72 ...';
EXEC SP_RENAME [dbo.TEMP72], [translation__protein_feature];
GO
;
PRINT '233 - CREATE INDEX I_78 ON homo_ ...';
CREATE INDEX I_78 ON homo_sapiens_vega_58_37c.dbo.translation__protein_feature(translation_id_1066);
GO
;
PRINT '234 - SELECT a.[translation_id_1 ...';
SELECT a.[translation_id_1066] INTO homo_sapiens_vega_58_37c.dbo.TEMP73 FROM homo_sapiens_vega_58_37c.dbo.transcript__translation AS a;
GO
;
PRINT '235 - CREATE INDEX I_79 ON homo_ ...';
CREATE INDEX I_79 ON homo_sapiens_vega_58_37c.dbo.TEMP73(translation_id_1066);
GO
;
PRINT '236 - SELECT a.*,b.[value] AS va ...';
SELECT a.*,b.[value] AS value_1067,b.[attrib_type_id] AS attrib_type_id_1067 INTO homo_sapiens_vega_58_37c.dbo.TEMP74 FROM homo_sapiens_vega_58_37c.dbo.TEMP73 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.translation_attrib AS b ON a.[translation_id_1066]=b.[translation_id];
GO
;
PRINT '237 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP73;
GO
;
PRINT '238 - CREATE INDEX I_80 ON homo_ ...';
CREATE INDEX I_80 ON homo_sapiens_vega_58_37c.dbo.TEMP74(attrib_type_id_1067);
GO
;
PRINT '239 - SELECT a.*,b.[description] ...';
SELECT a.*,b.[description] AS description_106,b.[name] AS name_106,b.[code] AS code_106 INTO homo_sapiens_vega_58_37c.dbo.TEMP75 FROM homo_sapiens_vega_58_37c.dbo.TEMP74 AS a INNER JOIN homo_sapiens_vega_58_37c.dbo.attrib_type AS b ON a.[attrib_type_id_1067]=b.[attrib_type_id];
GO
;
PRINT '240 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP74;
GO
;
PRINT '241 - CREATE INDEX I_81 ON homo_ ...';
CREATE INDEX I_81 ON homo_sapiens_vega_58_37c.dbo.TEMP75(translation_id_1066);
GO
;
PRINT '242 - SELECT a.[translation_id_1 ...';
SELECT a.[translation_id_1066],b.[attrib_type_id_1067],b.[code_106],b.[description_106],b.[name_106],b.[value_1067] INTO homo_sapiens_vega_58_37c.dbo.TEMP76 FROM homo_sapiens_vega_58_37c.dbo.transcript__translation AS a LEFT JOIN homo_sapiens_vega_58_37c.dbo.TEMP75 AS b ON a.[translation_id_1066]=b.[translation_id_1066];
GO
;
PRINT '243 - DROP TABLE homo_sapiens_ve ...';
DROP TABLE homo_sapiens_vega_58_37c.dbo.TEMP75;
GO
;
PRINT '244 - EXEC SP_RENAME [dbo.TEMP76 ...';
EXEC SP_RENAME [dbo.TEMP76], [translation__translation_attrib];
GO
;
PRINT '245 - CREATE INDEX I_82 ON homo_ ...';
CREATE INDEX I_82 ON homo_sapiens_vega_58_37c.dbo.translation__translation_attrib(translation_id_1066);
GO
;
