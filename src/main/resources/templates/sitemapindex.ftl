<?xml version='1.0' encoding='UTF-8'?>
<sitemapindex xmlns='http://www.sitemaps.org/schemas/sitemap/0.9'>
<#if maps gt 0>
    <#list 1..maps as i>
        <sitemap><loc>${apiUrl}v2/sitemap/taxon/${i?c}.txt</loc></sitemap>
    </#list>
</#if>
</sitemapindex>
