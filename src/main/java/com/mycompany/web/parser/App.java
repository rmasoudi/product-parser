package com.mycompany.web.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) throws IOException {
        Document brandDoc = Jsoup.connect("https://www.digikala.com/search/category-lipstick/?brand[0]=4593&pageno=" + 1 + "&sortby=4").get();
        Element el = brandDoc.selectFirst(".c-box--brands-filter");
        Elements brandEls = el.select("li");
        ObjectMapper mapper=new ObjectMapper();
        for (Element brandEl : brandEls) {
            Element label = brandEl.selectFirst("label");
            String fa = label.attr("data-fa");
            String en = label.attr("data-en");
            String brand = label.attr("for").replace("brand-param-", "");
            int counter = 0;
            HashMap<String,Object> brandItem=new HashMap<String, Object>();
            brandItem.put("farsiName", fa);
            brandItem.put("englishName", en);
            brandItem.put("id", brand);
            List<HashMap> brandProducts=new ArrayList<HashMap>();
            for (int page = 1; page < 500; page++) {
                Document doc;
                try {
                    doc = Jsoup.connect("https://www.digikala.com/search/category-lipstick/?brand[0]=" + brand + "&pageno=" + page + "&sortby=4").get();
                } catch (Exception e) {
                    break;
                }
                Elements products = doc.select(".c-product-box");
                for (Element product : products) {
                    counter++;
                    Element productLink = product.selectFirst(".c-product-box__img");
                    String url = productLink.attr("href");
                    Element productImage = productLink.selectFirst("img");
                    String title = productImage.attr("alt");
                    String imageUrl = productImage.attr("src");
                    BufferedImage image = ImageIO.read(new URL(imageUrl));
                    String imageId = brand + "_" + counter;
                    ImageIO.write(image, "jpg", new File("D:\\brands\\images\\" + imageId + ".jpg"));
                    String color = "";
                    try {
                        color = getColor(image, 445, 433);
                        if ("#ffffff".equals(color)) {
                            color = getColor(image, 365, 417);
                        }
                        if ("#ffffff".equals(color)) {
                            color = getColor(image, 436, 436);
                        }
                        if ("#ffffff".equals(color)) {
                            color = getColor(image, 427, 392);
                        }
                        if ("#ffffff".equals(color)) {
                            System.out.println(page + "  " + title);
                        }
                        if ("#ffffff".equals(color)) {
                            color = getColor(image, 345, 193);
                        }
                    } catch (Exception e) {
                        System.out.println(page + "------" + title);
                    }
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put("color", color);
                    item.put("title", title);
                    item.put("url", "https://www.digikala.com" + url);
                    item.put("imageId", imageId);
                    item.put("properties", getProductProperties(url));
                    brandProducts.add(item);
                }
            }
            brandItem.put("products", brandProducts);
            mapper.writeValue(new File("D:\\brands\\"+en+".json"), brandItem);
        }
    }

    private static HashMap<String, String> getProductProperties(String url) throws IOException {
        HashMap<String, String> result = new HashMap<String, String>();
        Document productPage = Jsoup.connect("https://www.digikala.com" + url).get();
        Element product = productPage.selectFirst(".c-params__list");
        Elements lis = product.select("li");
        for (Element li : lis) {
            String key = li.selectFirst(".c-params__list-key").text().trim();
            String value = li.selectFirst(".c-params__list-value").text().trim();
            result.put(key, value);
        }
        return result;
    }

    //365 417
    private static String getColor(BufferedImage image, int x, int y) {
        int clr = image.getRGB(x, y);
        int red = (clr & 0x00ff0000) >> 16;
        int green = (clr & 0x0000ff00) >> 8;
        int blue = clr & 0x000000ff;
        return String.format("#%02x%02x%02x", red, green, blue);
    }
}
