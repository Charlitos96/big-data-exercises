
package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * @author carlo
 */
public class MovieRecommender {
    
    private UserBasedRecommender recommender;
    private HashMap<String, Integer> users = new HashMap();
    private HashBiMap<String, Integer> products = HashBiMap.create();
    private int totalReviews = 0, totalUsers = 0, totalProducts = 0;
    
    // Constructor
    public MovieRecommender(String path) throws IOException, TasteException {
        inicialize(path);
    }
    
    private void inicialize(String path) throws FileNotFoundException, IOException, TasteException {
        String pathWriter = "D:/Charlitos/Nearsoft/programs/Semana_3/movies.csv";
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter wr = new BufferedWriter(new FileWriter(pathWriter));
        
        String userId = "", productId = "", score, line;
        int currentUser = 0, currentProduct = 0;
        
        while ((line = br.readLine()) != null) {
          switch(line.split(" ")[0]) {
              case "product/productId:":
                  productId = line.split(" ")[1];
                  if (!products.containsKey(productId)) {
                    totalProducts++;
                    products.put(productId,totalProducts);
                    currentProduct = totalProducts;
                  }else{
                    currentProduct = products.get(productId);
                  }
                  break;
              case "review/userId:":
                  userId = line.split(" ")[1];
                  if (!users.containsKey(userId)) {
                    totalUsers++;
                    users.put(userId,totalUsers);
                    currentUser = totalUsers;
                  }else{
                    currentUser = users.get(userId);
                  }
                  break;
              case "review/score:":
                  score = line.split(" ")[1];
                  wr.write(currentUser+","+currentProduct+","+score+"\n");
                  totalReviews++;
                  break;
          }
        }
        br.close();
        wr.close();
        DataModel model = new FileDataModel(new File(pathWriter));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    }
    
    public int getTotalReviews() {
        return totalReviews;
    }
    
    public int getTotalProducts() {
        return totalProducts;
    }
    
    public int getTotalUsers() {
        return totalUsers;
    }
    
    public List<String> getRecommendationsForUser(String userId) throws TasteException {
        List<String> list = new ArrayList<String>();
        int id = users.get(userId);
        List<RecommendedItem> recommendations = recommender.recommend(id, 3);
        BiMap<Integer, String> inverseProducts = products.inverse();
        for (RecommendedItem recommendation: recommendations) {
            list.add(inverseProducts.get((int) recommendation.getItemID()));
        }
        return list;
    }
    
}
