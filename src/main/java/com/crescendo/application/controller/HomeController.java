package com.crescendo.application.controller;

import com.crescendo.application.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.cloud.vision.v1.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class HomeController {

    private String searchQuery = "lazy bastard makati";
    private String baseUrl = "https://www.yelp.com/";

    private static final String CONSTANT_PLURAL_FRIEND = " friends";
    private static final String CONSTANT_SING_FRIEND = " friend";
    private static final String CONSTANT_PLURAL_REVIEW = " reviews";
    private static final String CONSTANT_SING_REVIEW = " reviews";
    private static final String CONSTANT_PLURAL_PHOTO = " photos";
    private static final String CONSTANT_SING_PHOTO = " photo";
    private static final String CONSTANT_STAR_RATING = " star rating";

    @GetMapping
    public List<String> home() {

        WebClient client = new WebClient();
        ArrayList<String> result = new ArrayList<>();

        try {
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);

            String searchUrl = baseUrl + "biz/" + formatUrlEncode(searchQuery);

            HtmlPage page = client.getPage(searchUrl);

            List<HtmlElement> items = page.getByXPath("//div[@class='review review--with-sidebar']");

            if(items.isEmpty()){
                result.add("No items found !");
            }else{
                for(HtmlElement htmlItem : items){

                    HtmlAnchor userName = (htmlItem.getFirstByXPath(".//li/a[@id='dropdown_user-name']"));
                    HtmlElement address = (htmlItem.getFirstByXPath(".//li[@class='user-location responsive-hidden-small']"));
                    HtmlElement friendCount = (htmlItem.getFirstByXPath(".//li[@class='friend-count responsive-small-display-inline-block']"));
                    HtmlElement reviewCount = (htmlItem.getFirstByXPath(".//li[@class='review-count responsive-small-display-inline-block']"));
                    HtmlElement photoCount = (htmlItem.getFirstByXPath(".//li[@class='photo-count responsive-small-display-inline-block']"));
                    HtmlImage rating = (htmlItem.getFirstByXPath(".//img[@class='offscreen']"));
                    HtmlElement reviewDate = (htmlItem.getFirstByXPath(".//span[@class='rating-qualifier']"));
                    HtmlElement comment = (htmlItem.getFirstByXPath(".//p"));
                    HtmlImage avatarImg = (htmlItem.getFirstByXPath(".//img[@class='photo-box-img']"));

                    String avatarImgSrc = avatarImg.getSrcAttribute();

                    URI uriImgSrc = new URI(avatarImgSrc);

                    Review review = new Review();

                    review.setUserName(userName.asText());
                    review.setAddress(address.asText());

                    if(friendCount != null && friendCount.asText().contains(CONSTANT_PLURAL_FRIEND)) {
                        review.setFriendCount(StringUtils.remove(friendCount.asText(), CONSTANT_PLURAL_FRIEND));
                    } else if(friendCount.asText().contains(CONSTANT_SING_FRIEND)) {
                        review.setFriendCount(StringUtils.remove(friendCount.asText(), CONSTANT_SING_FRIEND));
                    }

                    if(reviewCount != null && reviewCount.asText().contains(CONSTANT_PLURAL_REVIEW)) {
                        review.setReviewCount(StringUtils.remove(reviewCount.asText(), CONSTANT_PLURAL_REVIEW));
                    } else if(reviewCount.asText().contains(CONSTANT_SING_REVIEW)) {
                        review.setReviewCount(StringUtils.remove(reviewCount.asText(), CONSTANT_SING_REVIEW));
                    }

                    if(photoCount != null && photoCount.asText().contains(CONSTANT_PLURAL_PHOTO)) {
                        review.setPhotoCount(StringUtils.remove(photoCount.asText(), CONSTANT_PLURAL_PHOTO));
                    } else if(photoCount != null && photoCount.asText().contains(CONSTANT_SING_PHOTO)) {
                        review.setPhotoCount(StringUtils.remove(photoCount.asText(), CONSTANT_SING_PHOTO));
                    }

                    if(rating != null && rating.getAltAttribute().contains(CONSTANT_STAR_RATING)) {
                        review.setRating(StringUtils.remove(rating.getAltAttribute(), CONSTANT_STAR_RATING));
                    }

                    if(reviewDate != null) {
                        review.setReviewDate(reviewDate.asText());
                    }

                    if(comment != null) {
                        review.setComment(comment.asText());
                    }

                    detectFaces(uriImgSrc.toString(), System.out, review);

                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(review);

                    System.out.println(jsonString);

                    result.add(jsonString);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }

            return result;
    }

    private String formatUrlEncode(String encodedUrl) {

        return encodedUrl.replace(" ", "-");
    }

    public static void detectFaces(String gcsPath, PrintStream out, Review review) throws Exception,
            IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    out.printf("Error: %s\n", res.getError().getMessage());
                    return;
                }

                for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {

                    review.setAnger(annotation.getAngerLikelihood().toString());
                    review.setJoy(annotation.getJoyLikelihood().toString());
                    review.setSuprise(annotation.getSurpriseLikelihood().toString());
                    review.setSorrow(annotation.getSorrowLikelihood().toString());

                }
            }
        }
    }


}
