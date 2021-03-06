#!/bin/bash
rm out/test/*
rmdir out/test
rmdir out

export CLASSPATH=target/classes/:lib/*:import/
#java com.astav.jsontojava.JsonToJava sample.json out test TestOutput regex-sample.json false

#-------------- hasoffers api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffers/findAllOfferGroups.json out ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroups FindAllOfferGroups regex-sample.json false

#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffers/findAllOfferGroupOfferIds.json out ejb.bl.offerProviders.hasoffers.serde.findAllOfferGroupOfferIds FindAllOfferGroupOfferIds regex-sample.json false

#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffers/findOfferById.json out ejb.bl.offerProviders.hasoffers.serde.findOfferById FindOfferById regex-sample.json false
#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffers/findOfferByIdExtended.json out ejb.bl.offerProviders.hasoffers.serde.findOfferByIdExtended FindOfferByIdExtended regex-sample.json false

#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffers/getOfferThumbnail.json out ejb.bl.offerProviders.hasoffers.serde.getOfferThumbnail GetOfferThumbnail regex-sample.json false

#-------------- fyber api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/fyber/getOffers.json out is.iWeb.sentinel.data.dao.serde.fyber.getOffers GetOffers regex-sample.json false

#-------------- minimob api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/minimob/getOfferIdsOutput.json out ejb.bl.offerProviders.minimob.getOffersIds MinimobOfferIds regex-sample.json false

#java com.astav.jsontojava.JsonToJava jsonTemplates/minimob/getOfferByIdOutput.json out ejb.bl.offerProviders.minimob.getOfferById MinimobOffer regex-sample.json false

#-------------- aarki api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/aarki/getOffersOutput.json out ejb.bl.offerProviders.aarki.getOffers AarkiOffer regex-sample.json false

#-------------- supersonic api -----------------

#java com.astav.jsontojava.JsonToJava jsonTemplates/supersonic/getOffersOutput.json out ejb.bl.offerProviders.supersonic.getOffers SupersonicGetOffers regex-sample.json false

#java com.astav.jsontojava.JsonToJava jsonTemplates/test/test.txt out test.Test Test regex-sample.json false

#-------------- woobi-android api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/woobiAndroid/getOffersOutput.json out ejb.bl.offerProviders.woobi.getAndroidOffers WoobiAndroidGetOffers regex-sample.json false

#-------------- woobi-iOS api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/woobiIOS/getOffersOutput.json out ejb.bl.offerProviders.woobi.getIOSOffers WoobiIOSGetOffers regex-sample.json false

#-------------- hasoffersExt api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffersExt/getOffer.json out ejb.bl.offerProviders.hasoffersExt.getOffer GetOffer regex-sample.json false
#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffersExt/getThumbnail.json out ejb.bl.offerProviders.hasoffersExt.getThumbnail GetThumbnail regex-sample.json false
#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffersExt/getCountry.json out ejb.bl.offerProviders.hasoffersExt.getCountry GetCountry regex-sample.json false
#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffersExt/generateTrackingLink.json out ejb.bl.offerProviders.hasoffersExt.getTrackingLink GetTrackingLink regex-sample.json false
#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffersExt/getPayoutDetails.json out ejb.bl.offerProviders.hasoffersExt.getPayoutDetails GetPayoutDetails regex-sample.json false
#java com.astav.jsontojava.JsonToJava jsonTemplates/hasoffersExt/getRuleTargetingForOffer.json out ejb.bl.offerProviders.hasoffersExt.getRuleTargetingForOffer GetRuleTargetingForOffer regex-sample.json false

#-------------- clickky api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/clickky/getOffers.json out ejb.bl.offerProviders.clickey.getOffers GetOffers regex-sample.json false

#-------------- trialpay api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/trialpay/getOffers.json out ejb.bl.offerProviders.trialpay.getOffers TrialPayOffer regex-sample.json false

#-------------- personaly api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/personaly/getOffers.json out ejb.bl.offerProviders.personaly.getOffers GetOffers regex-sample.json false

#-------------- snapdeal api -----------------
#java com.astav.jsontojava.JsonToJava jsonTemplates/snapdeal/GetCategories.json out ejb.bl.offerProviders.snapdeal.getCategories GetCategories regex-sample.json false
#java com.astav.jsontojava.JsonToJava jsonTemplates/snapdeal/CategoryOffers.json out ejb.bl.offerProviders.snapdeal.CategoryOffers CategoryOffers regex-sample.json false


#-------------- fyber api -----------------
java com.astav.jsontojava.JsonToJava jsonTemplates/adGate/GetAdGateOffers.json out is.ejb.bl.offerProviders.adgate.getAdGateOffers GetAdGateOffers regex-sample.json false


