<!doctype html>
<%@ page language="java"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="biomart" tagdir="/WEB-INF/tags" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="currentPage" scope="request">
  Dataset Summary
</c:set>
<html>
<head>
  <c:import url="/conf/config.jsp" context="/"/>
  <title>${requestScope.currentPage}</title>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

  <c:import url="/_head.jsp?path=../../" context="/"/>

  <link type="text/css" href="css/summary-table.css" rel="stylesheet" />
</head>
<!--[if lt IE 7 ]> <body class="biomart layout1 main ie6 "> <![endif]--> 
<!--[if IE 7 ]>    <body class="biomart layout1 main ie7 "> <![endif]--> 
<!--[if IE 8 ]>    <body class="biomart layout1 main ie8 "> <![endif]--> 
<!--[if !IE]><!--> <body class="biomart layout1 main"> <!--<![endif]--> 

<div id="biomart-top-wrapper">
  <div id="biomart-header">
    <c:import url="/_header.jsp?path=../../" context="/"/>
  </div>

  <c:import url="/_context.jsp?path=../../" context="/"/>

  <div id="biomart-wrapper">
    <div id="biomart-content" class="clearfix">

<h3 class="ui-widget-header ui-corner-top">
Dataset Summary
</h3>

<table class='dataset-summary'>
    <tbody>
        <tr>
            <th rowspan=2 style='border: 0px;' class='verticall'>Source</th>
            <th rowspan=2 width='30%'>Cancer Project</th>
            <th rowspan=2>Tissue</th>
            <th colspan=10>Dataset</th>
        </tr>
        <tr>
            <%--            <th>Source</th> --%>
            <th>Donors</th>
            <th>Samples</th>
            <th>Structural Rearrangements</th>
            <th>Gene Expression</th>
            <th>CNV</th>
            <th>Simple Mutations</th>
            <th>miRNA Expression</th>
            <th>Exon Junction</th>
            <th>DNA Methylation</th>
            <th>Germline Variations<a href='#controlled_access'><sup>*</sup></a></th>
        </tr>
 
        <tr class='center_icgc top-row'>
            <td class='spacer-cell'></td>
            <td class='dataset_blood project-name' bgcolor="orange">Acute Myeloid Leukemia <span class='project-center'>(TCGA, US)</span></td>
            <td class='dataset_blood tissue' bgcolor="CornflowerBlue">Blood</td>
            <td align="center" bgcolor="DarkSeaGreen">202</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaLAML'>188</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaLAML'>188</a></td>
            <td><p class="empty">-</p></td>
        </tr>


        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>ICGC</td> --%>
            <td class='dataset_blood project-name' bgcolor="orange">Chronic Lymphocytic Leukemia <span class='project-center'>(ISC/MICINN, ES)</td>
            <td class='dataset_blood tissue' bgcolor="CornflowerBlue">Blood</td>
            <td align="center" bgcolor="DarkSeaGreen">4</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_esCLL'>4</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/cnv_config_quick?ds=cnv_esCLL'>4</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_esCLL'>4</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>

        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>TCGA</td> --%>
            <td class='dataset_brain project-name' bgcolor="orange">Glioblastoma Multiforme <span class='project-center'>(TCGA, US)<sup>1</sup></td>
            <td class='dataset_brain tissue' bgcolor="Crimson">Brain</td>
            <td align="center" bgcolor="DarkSeaGreen">512</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaGBM'>378</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_tcgaGBM'>243</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_tcgaGBM'>147</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaGBM'>258</a></td>
            <td><p class="empty">-</p></td>
        </tr>



        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <td class='dataset_breast project-name' bgcolor="orange">Breast Carcinoma <span class='project-center'>(WTSI, UK)</td>
            <td class='dataset_breast tissue' bgcolor="Olive">Breast</td>
            <td align="center" bgcolor="DarkSeaGreen">24</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_sangerBreast'>24</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sv_config_quick?ds=sv_sangerBreast'>24</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
             <%--        <td>TCGA</td> --%>
            <td class='dataset_breast project-name' bgcolor="orange">Breast Invasive Carcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_breast tissue' bgcolor="Olive">Breast</td>
            <td align="center" bgcolor="DarkSeaGreen">436</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaBRCA'>372</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_tcgaBRCA'>371</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaBRCA'>186</a></td>
            <td><p class="empty">-</p></td>
        </tr>


        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>TCGA</td> --%>
            <td class='dataset_colon project-name' bgcolor="orange">Colon Adenocarcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_colon tissue' bgcolor="MediumOrchid">Colon</td>
            <td align="center" bgcolor="DarkSeaGreen">173</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaCOAD'>186</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_tcgaCOAD'>155</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaCOAD'>185</a></td>
            <td><p class="empty">-</p></td>
        </tr>


        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
<%--            <td>TCGA</td> --%>
            <td class='dataset_kidney project-name' bgcolor="orange">Kidney Renal Clear Cell Carcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_kidney tissue' bgcolor="DeepSkyBlue">Kidney</td>
            <td align="center" bgcolor="DarkSeaGreen">356</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaKIRC'>420</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_tcgaKIRC'>41</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaKIRC'>419</a></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>TCGA</td> --%>
            <td class='dataset_kidney project-name' bgcolor="orange">Kidney Renal Papillary Cell Carcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_kidney tissue' bgcolor="DeepSkyBlue">Kidney</td>
            <td align="center" bgcolor="DarkSeaGreen">17</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaKIRP'>22</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaKIRP'>22</a></td>
            <td><p class="empty">-</p></td>
        </tr>


        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
<%--            <td>ICGC</td> --%>
            <td class='dataset_liver project-name' bgcolor="orange">Liver Cancer <span class='project-center'>(NCC, JP)</td>
            <td class='dataset_liver tissue' bgcolor="GoldenRod">Liver</td>
            <td align="center" bgcolor="DarkSeaGreen">1</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_jpNCCLiver'>1</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sv_config_quick?ds=sv_jpNCCLiver'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_jpNCCLiver'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_icgc'>
            <td class='verticall'>ICGC</td>
            <%--        <td>ICGC</td> --%>
            <td class='dataset_liver project-name' bgcolor="orange">Liver Cancer <span class='project-center'>(RIKEN, JP)</td>
            <td class='dataset_liver tissue' bgcolor="GoldenRod">Liver</td>
            <td align="center" bgcolor="DarkSeaGreen">1</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_jpRikenLiver'>1</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sv_config_quick?ds=sv_jpRikenLiver'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_jpRikenLiver'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>

        
        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%-- <td class='verticall'>TCGA</td> --%>
            <%--        <td>TCGA</td>  --%>
            <td class='dataset_lung project-name' bgcolor="orange">Lung Adenocarcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_lung tissue' bgcolor="LightSkyblue">Lung</td>
            <td align="center" bgcolor="DarkSeaGreen">135</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaLUAD'>154</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_tcgaLUAD'>32</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaLUAD'>153</a></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>TCGA</td>  --%>
            <td class='dataset_lung project-name' bgcolor="orange">Lung Squamous Cell Carcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_lung tissue' bgcolor="LightSkyblue">Lung</td>
            <td align="center" bgcolor="DarkSeaGreen">163</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaLUSC'>162</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_tcgaLUSC'>134</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaLUSC'>160</a></td>
            <td><p class="empty">-</p></td>
        </tr>

        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>ICGC</td> --%>
            <td class='dataset_lung project-name' bgcolor="orange">Small Cell Lung Carcinoma <span class='project-center'>(WTSI, UK)</td>
            <td class='dataset_lung tissue' bgcolor="LightSkyblue">Lung</td>
            <td align="center" bgcolor="DarkSeaGreen">1</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_sangerLung'>1</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sv_config_quick?ds=sv_sangerLung'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_sangerLung'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>

        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>TCGA</td>  --%>
            <td class='dataset_ovary project-name' bgcolor="orange">Ovarian Serous Cystadenocarcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_ovary tissue' bgcolor="IndianRed">Ovary</td>
            <td align="center" bgcolor="DarkSeaGreen">594</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaOV'>546</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_tcgaOV'>525</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_tcgaOV'>185</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaOV'>545</a></td>
            <td><p class="empty">-</p></td>
        </tr>



        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--    <td>ICGC</td> --%>
            <td class='dataset_pancreas project-name' bgcolor="orange">Pancreatic Cancer <span class='project-center'>(OICR, CA)</td>
            <td class='dataset_pancreas tissue' bgcolor="LimeGreen">Pancreas</td>
            <td align="center" bgcolor="DarkSeaGreen">5</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_oicrPanc'>3</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_oicrPanc'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/mirna_config_quick?ds=mirna_oicrPanc'>3</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td>1<a href='#controlled_access'><sup>*</sup></a></td>
        </tr>
        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
<%--            <td>ICGC</td> --%>
            <td class='dataset_pancreas project-name' bgcolor="orange">Pancreatic Cancer <span class='project-center'>(QCMG, AU)</td>
            <td class='dataset_pancreas tissue' bgcolor="LimeGreen">Pancreas</td>
            <td align="center" bgcolor="DarkSeaGreen">5</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_QCMGPancreas'>9</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sv_config_quick?ds=sv_QCMGPancreas'>3</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_QCMGPancreas'>3</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/cnv_config_quick?ds=cnv_QCMGPancreas'>3</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_QCMGPancreas'>3</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/mirna_config_quick?ds=mirna_QCMGPancreas'>6</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/jcn_config_quick?ds=jcn_QCMGPancreas'>3</a></td>
            <td><p class="empty">-</p></td>
            <td>3<a href='#controlled_access'><sup>*</sup></a></td>
        </tr>

        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>TCGA</td>  --%>
            <td class='dataset_rectum project-name' bgcolor="orange">Rectum Adenocarcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_rectum tissue' bgcolor="MediumPurple">Rectum</td>
            <td align="center" bgcolor="DarkSeaGreen">83</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaREAD'>73</a></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/exp_config_quick?ds=exp_tcgaREAD'>69</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaREAD'>73</a></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
<%--            <td>ICGC</td> --%>
            <td class='dataset_skin project-name' bgcolor="orange">Malignant Melanoma <span class='project-center'>(WTSI, UK)</td>
            <td class='dataset_skin tissue' bgcolor="LightBlue">Skin</td>
            <td align="center" bgcolor="DarkSeaGreen">1</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_sangerMelanoma'>1</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sv_config_quick?ds=sv_sangerMelanoma'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_sangerMelanoma'>1</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>TCGA</td> --%>
            <td class='dataset_stomach project-name' bgcolor="orange">Stomach Adenocarcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_stomach tissue' bgcolor="LightSalmon">Stomach</td>
            <td align="center" bgcolor="DarkSeaGreen">83</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaSTAD'>142</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaSTAD'>142</a></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_icgc'>
            <td class='spacer-cell'></td>
            <%--        <td>TCGA</td> --%>
            <td class='dataset_uterus project-name' bgcolor="orange">Uterine Corpus Endometrioid Carcinoma <span class='project-center'>(TCGA, US)</td>
            <td class='dataset_uterus tissue' bgcolor="LightSteelBlue">Uterus</td>
            <td align="center" bgcolor="DarkSeaGreen">193</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tcgaUCEC'>71</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/meth_config_quick?ds=meth_tcgaUCEC'>71</a></td>
            <td><p class="empty">-</p></td>
        </tr>

        <tr class='center_other top-row'>
            <td class='verticall'>Other</td>
            <%--        <td>Other</td> --%>
            <td class='dataset_brain project-name' bgcolor="orange">Glioblastoma Multiforme <span class='project-center'>(JHU, US)<sup>4</sup></td>
            <td class='dataset_brain tissue' bgcolor="Crimson">Brain</td>
            <td align="center" bgcolor="DarkSeaGreen">106</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_hopkinsGBM'>90</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/cnv_config_quick?ds=cnv_hopkinsGBM'>22</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_hopkinsGBM'>89</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_other'>
             <td class='spacer-cell'></td>
            <td class='dataset_breast project-name' bgcolor="orange">Breast Cancer <span class='project-center'>(JHU, US)<sup>6,7</sup></td>
            <td class='dataset_breast tissue' bgcolor="Olive">Breast</td>
            <td align="center" bgcolor="DarkSeaGreen">48</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_hopkinsBreast'>42</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_hopkinsBreast'>42</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_other'>
            <td class='spacer-cell'></td>
            <%--        <td>Other</td> --%>
            <td class='dataset_colon project-name' bgcolor="orange">Colorectal Cancer <span class='project-center'>(JHU, US)<sup>6,7</sup></td>
            <td class='dataset_colon tissue' bgcolor="MediumOrchid">Colon</td>
            <td align="center" bgcolor="DarkSeaGreen">37</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_hopkinsColon'>36</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_hopkinsColon'>36</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_other'>
            <td class='spacer-cell'></td>
<%--            <td>Other</td> --%>
            <td class='dataset_lung project-name' bgcolor="orange">Lung Adenocarcinoma <span class='project-center'>(TSP, US)<sup>2</sup></td>
            <td class='dataset_lung tissue' bgcolor="LightSkyblue">Lung</td>
            <td align="center" bgcolor="DarkSeaGreen">188</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_tspLung'>163</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_tspLung'>163</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>
        <tr class='center_other bottom-row'>
            <td class='spacer-cell'></td>
            <%--        <td>Other</td> --%>
            <td class='dataset_pancreas project-name' bgcolor="orange">Pancreatic Cancer <span class='project-center'>(JHU, US)<sup>3</sup></td>
            <td class='dataset_pancreas tissue' bgcolor="LimeGreen">Pancreas</td>
            <td align="center" bgcolor="DarkSeaGreen">114</td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/sample_config_quick?ds=sample_hopkinsPanc'>114</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/cnv_config_quick?ds=cnv_hopkinsPanc'>24</a></td>
            <td align="center" bgcolor="cyan"><a href='/martanalysis/#!/Quick/snp_config_quick?ds=snp_hopkinsPanc'>114</a></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
            <td><p class="empty">-</p></td>
        </tr>

    </tbody>
</table>

<br />
<p>
<sup>*</sup><a name='controlled_access'></a>'Germline Variations’ is a controlled dataset. <img src='/pages/images/closed_access.png' height='15px' border=0 style='vertical-align: middle'> <a href='http://icgc.org/daco'>Click here to apply for access to controlled data</a>. 
</p>
<br />
<ul class='citation_list'>
<li>
<sup>1</sup>
Cancer Genome Atlas Research Network. (2008) <em><a href='http://www.ncbi.nlm.nih.gov/pubmed/18772890' target='_blank'>Comprehensive genomic characterization defines human glioblastoma genes and core pathways</a></em>. Nature, 455 (7216), 1061-8.
</li>
<li>
<sup>2</sup>
Ding L., et al. (2008) <em><a href='http://www.ncbi.nlm.nih.gov/pubmed/18948947' target='_blank'>Somatic mutations affect key pathways in lung adenocarcinoma</a></em>. Nature. 455 (7216): 14069-1075
</li>
<li>
<sup>3</sup>
Jones S. et al. (2008) <em><a href='http://www.ncbi.nlm.nih.gov/pubmed/18772397' target='_blank'>Core signaling pathways in human pancreatic cancers revealed by global genomic analyses</a></em>. Science, 321 (5897): 1801-1806.
</li>
<li>
<sup>4</sup>
Parsons, DW., et al. (2008) <em><a href='http://www.ncbi.nlm.nih.gov/pubmed/18772396' target='_blank'>An integrated genomic analysis of human glioblastoma multiforme</a></em>. Science. 321 (5897): 1807-1812
</li>
<li>
<sup>5</sup>
Pleasance, E.D., et al. (2010) <em><a href='http://www.ncbi.nlm.nih.gov/pubmed/20016488' target='_blank'>A small-cell lung cancer genome with complex signatures of tobacco exposure</a></em>. Nature. 463: 184-190.
</li>
<li>
<sup>6</sup>
Sjoblom, T., et al. (2006) <em><a href='http://www.ncbi.nlm.nih.gov/pubmed/16959974' target='_blank'>The consensus coding sequences of human breast and colorectal cancers</a></em>. Science. 314: 268-274.
</li>
<li>
<sup>7</sup>
Wood. LD., et al. (2007) <em><a href='http://www.ncbi.nlm.nih.gov/pubmed/17932254' target='_blank'>The genomic landscapes of human breast and colorectal cancers</a></em>. Science. 318: 1108-1113.
</li>
</ul>

    <div id="biomart-content-footer" class="clearfix">
      <p class="version">Powered by <a href="http://www.biomart.org/" title="Visit biomart.org">BioMart</a></p>
    </div>
  </div>

  <div id="biomart-footer">
    <c:import url="/_footer.jsp?path=../../" context="/"/>
  </div>
</div>

<c:import url="/_js_includes.jsp?path=../../" context="/"/>

<script type="text/javascript">
  // Do not touch this
    $(document).ready(function() {
        $.publish('biomart.login'); 
    $.subscribe('biomart.restart', {refresh:function(){location=location.href}}, 'refresh');
    });
</script>

</body>
</html>
