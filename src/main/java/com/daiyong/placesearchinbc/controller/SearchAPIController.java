package com.daiyong.placesearchinbc.controller;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
public class SearchAPIController {
    //   Prepare output in JSON format
//https://maps.googleapis.com/maps/api/place/textsearch/json?query=restaurants+in+Coquitlam&key=AIzaSyDyuHc9QSqGV3msVyAMj0y7Q6SavU8q9kw
//ATtYBwKulaqE_55fdi1evUGCgNu8DqHx-DNtKePvY_SZvLD2kG6TwXOqVhK1XHbxEZO9oADjaH83ygA1yXSznfXE3akoH4KJETSoJh-gXjLawA5cEAcGCZZH8HOxenTTSq99V0wswKac67dL6oVlEIKmTI41sdEgJEXrX9MX_DTtGkWbY6f1aLxVGyAvHztkkUFIsIBEF6SVjjiv_a36XG5yIl79cdHLASCVmp0w3fJa7tRi1e24ATEtFyrrHFmnKXw0nkOxygajkJn7THrjIv-fvyF4YVBPAtPG2jLdMBoFCkBS7qraRHX7hgi0hwbiMYju8Ev6wm1jsS4e-VQu4Oo2jOiStoqFXPFOKHUxEce9txpJ6yPz2FDrIzpmVezvXCAedV1oMmcYr51xLFGSXuFtQU9--uCaC-gPshIWKVZSQnlXQ8c

    @RequestMapping(path="/place_get", produces= MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String processForm(@RequestParam(defaultValue="keyword") String keyword) throws ParseException {
        List<String> cities = GetCitiesList();
        //System.out.println(cities);
        String next_page_token = null;
        Object resultsArray = null;
        List<Object> results = new ArrayList<Object>();
        List<String> tmp = null;
        String loop = null;

        // TO DO : All city list
        for(String city:cities)
        {
            // it can be beautify using formatter
            String uri = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="
                    + keyword
                    + " in"
                    + city
                    + "&key=AIzaSyDyuHc9QSqGV3msVyAMj0y7Q6SavU8q9kw";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(uri, String.class);

            Object obj = new JSONParser().parse(result);
            JSONObject jo = (JSONObject) obj;
            next_page_token = (String) jo.get("next_page_token");
            resultsArray = (ArrayList) jo.get("results");

            for (Object ob : (ArrayList) resultsArray)
                results.add(ob);

            while (loop != null) {
                //uri = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + keyword + " in "+ "coquitlam" + "&key=AIzaSyDyuHc9QSqGV3msVyAMj0y7Q6SavU8q9kw&next_page_token=" + next_page_token;
                // it can be beautify using formatter
                uri = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="
                        + keyword
                        + " in "
                        + city
                        + "&key=AIzaSyDyuHc9QSqGV3msVyAMj0y7Q6SavU8q9kw&next_page_token="
                        + next_page_token;

                result = restTemplate.getForObject(uri, String.class);


                obj = new JSONParser().parse(result);
                jo = (JSONObject) obj;
                next_page_token = (String) jo.get("next_page_token");
                resultsArray = (ArrayList) jo.get("results");

                for (Object ob : (ArrayList) resultsArray)
                    results.add(ob);
                loop = (String) next_page_token;

            }
        }

        String place_id = null;
        JSONObject plus_code = null;
        String name = null;
        String formatted_address = null;
        Map<String, String> hashMap = new HashMap<>();
        List<JSONObject> ourResult = new ArrayList<JSONObject>();

        for(Object result:results)
        {

            System.out.println(result);

            JSONObject jo = (JSONObject)result;

            name = (String) jo.get("name");
            formatted_address = (String) jo.get("formatted_address");

            if(!hashMap.containsKey(formatted_address)) {
                //System.out.println(place_id);
                hashMap.put(place_id, "true");
                try {
                    JSONObject entity = new JSONObject();
                    entity.put("name", name);
                    entity.put("formatted_address", formatted_address);
                    ourResult.add(entity);
                } catch(Exception e)
                {
                    System.out.println(e);
                }
            }
        }

        System.out.println(ourResult.size());
        return String.format("%s",  ourResult);
    }

    List<String> GetCitiesList()
    {
        Path fileName = Paths.get("citiesinbc.txt");
        Charset charset = StandardCharsets.UTF_8;

        try {
            List<String> lines = Files.readAllLines(fileName, charset);
            return lines;
        } catch (IOException ex) {
            System.out.format("I/O error: %s%n", ex);
        }
        return null;
    }



}
