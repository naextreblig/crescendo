package com.crescendo.application.service;

import com.crescendo.application.model.Review;
import com.crescendo.application.model.ReviewParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.cloud.vision.v1.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class YelpService {

    private static final String baseUrl = "https://www.yelp.com/";
    private static final String CONSTANT_PLURAL_FRIEND = " friends";
    private static final String CONSTANT_SING_FRIEND = " friend";
    private static final String CONSTANT_PLURAL_REVIEW = " reviews";
    private static final String CONSTANT_SING_REVIEW = " reviews";
    private static final String CONSTANT_PLURAL_PHOTO = " photos";
    private static final String CONSTANT_SING_PHOTO = " photo";
    private static final String CONSTANT_STAR_RATING = " star rating";

    public List<Review> getReview(ReviewParam reviewParam) {

        WebClient client = new WebClient();
        ArrayList<Review> result = new ArrayList<>();

        try {
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);

            String searchUrl = baseUrl + "biz/" + formatUrlEncode(reviewParam.getRestaurantName());

            HtmlPage page = client.getPage(searchUrl);

            List<HtmlElement> items = page.getByXPath("//div[@class='review review--with-sidebar']");

            if(items.isEmpty()){
                return Collections.emptyList();
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

                    Review review = new Review();

                    URI uriImgSrc = new URI(avatarImgSrc);

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


                    result.add(review);
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
