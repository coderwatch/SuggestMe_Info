package edu.csupomona.cs480.controller;



//////////////////////////////////////////////////////////////////
//////////////////////LIBRARIES///////////////////////////////////
//////////////////////////////////////////////////////////////////
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import edu.csupomona.cs480.App;
import edu.csupomona.cs480.API.LocationAPI.*;
import edu.csupomona.cs480.API.EventAPI.EventAPI;
import edu.csupomona.cs480.API.EventAPI.EventBriteAPI;
import edu.csupomona.cs480.API.Food2Fork.Food2Fork;
import edu.csupomona.cs480.Events.Event;
import edu.csupomona.cs480.data.User;
import edu.csupomona.cs480.data.provider.UserManager;
import edu.csupomona.cs480.location.Location;
import edu.csupomona.cs480.location.Venue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;
import com.google.common.base.Joiner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math.fraction.Fraction;
import org.json.JSONObject;

import java.io.*;
///////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////

/**
 * This is the controller used by Spring framework.
 * <p>
 * The basic function of this controller is to map
 * each HTTP API Path to the correspondent method.
 *
 */

@RestController
public class WebController {
	
	
	public static Food2Fork fork = new Food2Fork();
	public static ArrayList<JSONObject> recipelist = new ArrayList<JSONObject>();
	public static Random rand = new Random();

	/**
	 * When the class instance is annotated with
	 * {@link Autowired}, it will be looking for the actual
	 * instance from the defined beans.
	 * <p>
	 * In our project, all the beans are defined in
	 * the {@link App} class.
	 */
	@Autowired
	private UserManager userManager;

	/**
	 * This is a simple example of how the HTTP API works.
	 * It returns a String "OK" in the HTTP response.
	 * To try it, run the web application locally,
	 * in your web browser, type the link:
	 * 	http://localhost:8080/cs480/ping
	 */

	@RequestMapping("/sway")
	String sway(){
		//returns html text
		return "<h1>Test Page</h1><a href=\"http://corgiorgy.com/\"><img src=\"http://45.media.tumblr.com/d9638010e1374a54620dbe2cd847f647/tumblr_o52vjkloZo1rnhl8xo1_500.gif\"></a><br><b>Test</b> ";
	}
	
	@RequestMapping(value = "/testjson", method = RequestMethod.GET)
	String testJSON() {
		
		String myJSON="";
		try{
			myJSON=Jsoup.connect("https://maps.googleapis.com/maps/api/place/textsearch/json?query=arcade+in+Los+Angeles&key=AIzaSyB8u4PGN6dz5-HbrY5aJZxwyzTkCfiFj8Q").ignoreContentType(true).execute().body();
		}
		catch(IOException e)
		{
			
		}
		return myJSON;
	}

	@RequestMapping(value = "/cs480/ping", method = RequestMethod.GET)
	String healthCheck() {
		// You can replace this with other string,
		// and run the application locally to check your changes
		// with the URL: http://localhost:8080/
		return "OK";
	}

	/**
	 * This is a simple example of how to use a data manager
	 * to retrieve the data and return it as an HTTP response.
	 * <p>
	 * Note, when it returns from the Spring, it will be
	 * automatically converted to JSON format.
	 * <p>
	 * Try it in your web browser:
	 * 	http://localhost:8080/cs480/user/user101
	 */
	@RequestMapping(value = "/cs480/user/{userId}", method = RequestMethod.GET)
	User getUser(@PathVariable("userId") String userId) {
		User user = userManager.getUser(userId);
		return user;
	}
	
	
	@RequestMapping(value = "/events/{latitude}/{longitude}", method = RequestMethod.GET)
	String getEvents(@PathVariable("latitude") double latitude, @PathVariable("longitude") double longitude) {
		EventAPI api = new EventBriteAPI();
		Location geoLocation = new Location(latitude, longitude);
		String eventsJson = api.getEventsJsonByGeoLocation(geoLocation);
		return eventsJson;	
	}
	
	@RequestMapping(value = "/getVenue/{id}", method = RequestMethod.GET)
	String getVenue(@PathVariable("id") int id){
		EventAPI api = new EventBriteAPI();
		Venue venue = new Venue(id);
		String venueJson = api.getVenueCoordinates(venue);
		return venueJson;
	}
	
	//<!------Using Yelp API------->
	@RequestMapping(value = "/food/{location}", method = RequestMethod.GET)
	String getLocation(@PathVariable("location") String location) throws IOException{
		YelpAPI yelp = new YelpAPI();
		yelp.setLocation(location);
		String jsonresponse = yelp.jsonresponse();
		return jsonresponse;
	}
	
	//<!-----Yelp Search with Longitude and Latitude----->
	@RequestMapping(value = "/food/{Latitude}/{Longitude}", method = RequestMethod.GET)
	String getLocationLL(@PathVariable("Latitude") String Latitude, 
						@PathVariable("Longitude") String Longitude) throws IOException{
		String LL = Latitude +","+Longitude;
		YelpAPI yelp = new YelpAPI();
		yelp.setLL(LL);
		String jsonresponse = yelp.lnljson();
		return jsonresponse;
	}
	
	//<!------Food2Fork Recipe List Search------>
	@RequestMapping(value = "/recipe/{search}", method = RequestMethod.GET)
	String getRecipeList(@PathVariable("search") String search)throws IOException{
		final JSONObject searchResults = fork.search(search);
		recipelist.clear();
		for(int i = 10; i < 10; i++){
			recipelist.add(fork.getRecipe(fork.getRecipeIds(searchResults).get(i)));
		}
		int index = (int) rand.nextInt(recipelist.size());
		String response = recipelist.get(index).toString(2);
		recipelist.remove(index);
		return response;
	}
	
	@RequestMapping(value = "/recipe/random", method = RequestMethod.GET)
	String getRandomRecipe()throws IOException{
		
		String response= null;
		if(recipelist.size() <= 0){
			response = "List is empty!";
		}
		else{
		
		int index = rand.nextInt(recipelist.size());
		response = recipelist.get(index).toString(2);
		recipelist.remove(index);
		}
		
		return response;
	}
	

	//Gets the user's name.
	@RequestMapping(value = "/cs480/user/name/{userId}", method = RequestMethod.GET)
	String getUserName(@PathVariable("userId") String userId) {
		return userManager.getUser(userId).getName();
	}

	/**
	 * This is an example of sending an HTTP POST request to
	 * update a user's information (or create the user if not
	 * exists before).
	 *
	 * You can test this with a HTTP client by sending
	 *  http://localhost:8080/cs480/user/user101
	 *  	name=John major=CS
	 *
	 * Note, the URL will not work directly in browser, because
	 * it is not a GET request. You need to use a tool such as
	 * curl.
	 *
	 * @param id
	 * @param name
	 * @param major
	 * @return
	 */
	@RequestMapping(value = "/cs480/user/{userId}", method = RequestMethod.POST)
	User updateUser(
			@PathVariable("userId") String id,
			@RequestParam("name") String name,
			@RequestParam(value = "major", required = false) String major) {
		User user = new User();
		user.setId(id);
		user.setMajor(major);
		user.setName(name);
		userManager.updateUser(user);
		return user;
	}
	@RequestMapping(value = "/cs480/test", method = RequestMethod.GET)
	String test(){

		return "test";
	}

	//test of the guava library for working and replacing nulls easily/////
	@RequestMapping(value = "/guava", method = RequestMethod.GET)
	String guavaNullChecker(){
		String[] helloWorldNullArray = {"Hello",null};
		String nullConcat = Joiner.on(", ").useForNull("World!").join(helloWorldNullArray);
		return nullConcat;
	}
	
//	Commons Math library test
	@RequestMapping(value = "/math/{numerator}/{denominator}", method = RequestMethod.GET)
	String showFractionSquared(
			@PathVariable("numerator") int numer,
			@PathVariable("denominator") int denom
			)
	{
		Fraction fract = new Fraction(numer, denom), 
				fractSquared = fract.multiply(fract);
		
		return "You entered " + fract.toString() + "; that fraction squared is " + fractSquared;
	}
	
	
	//<!----COMMONS IO TEST------>
	
	@RequestMapping(value = "/file", method = RequestMethod.GET)
	String commonsio() throws IOException{
		//uses commons io implementaion
		//creates text file then uses commons io to read file and print on webpage
		boolean check = (new File("demo.txt").delete());
		if(check){
			FileWriter writer = new FileWriter("demo.txt", true);
			writer.write("<h1>TEST GIF</h2>");
			writer.write("<img src=\"https://m.popkey.co/f446cc/qVq0a.gif\">");
			writer.close();
		}
		String ok = FileUtils.fileRead("demo.txt");
		return ok;
	}


	/**
	 * This API deletes the user. It uses HTTP DELETE method.
	 *
	 * @param userId
	 */
	@RequestMapping(value = "/cs480/user/{userId}", method = RequestMethod.DELETE)
	void deleteUser(
			@PathVariable("userId") String userId) {
		userManager.deleteUser(userId);
	}

	/**
	 * This API lists all the users in the current database.
	 *
	 * @return
	 */
	@RequestMapping(value = "/cs480/users/list", method = RequestMethod.GET)
	List<User> listAllUsers() {
		return userManager.listAllUsers();
	}

	/*********** Web UI Test Utility **********/
	/**
	 * This method provide a simple web UI for you to test the different
	 * functionalities used in this web service.
	 */
	@RequestMapping(value = "/cs480/home", method = RequestMethod.GET)
	ModelAndView getUserHomepage() {
		ModelAndView modelAndView = new ModelAndView("home");
		modelAndView.addObject("users", listAllUsers());
		return modelAndView;
	}

	@RequestMapping(value = "/cs480/google", method = RequestMethod.GET)
	String google(){
		String answer = null;
		Document doc;
		try {

			// need http protocol
			doc = Jsoup.connect("http://google.com").get();


			String title = doc.title();
			answer = answer + "title : " + title;

			Elements links = doc.select("a[href]");
			for (Element link : links) {
				answer = answer + "link: " + link.attr("href") + "   ";
				answer = answer + "text: " + link.text() + "   ";

			}
		} 

		catch (IOException e) {
			e.printStackTrace();
		}
		return answer;
	}
}