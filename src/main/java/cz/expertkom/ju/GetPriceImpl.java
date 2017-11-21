package cz.expertkom.ju;

import org.springframework.stereotype.Service;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import cz.expertkom.ju.interfaces.GetPriceInterface;
import cz.expertkom.ju.interfaces.entity.ProductPrice;

@Service
public class GetPriceImpl implements GetPriceInterface{
	
	private ProductPrice productPrice = new ProductPrice();

	public ProductPrice getProductPrice(String valueURI) {
			
		final String WEB_PAGE_DOWNLOAD = "https://"+valueURI;

		try {
			String stringDownloadedWebPage = Unirest.get(WEB_PAGE_DOWNLOAD).asString().getBody();
			String[] listStringSplit = stringDownloadedWebPage.split("'");
			boolean productFound = false;
			boolean priceVATfound = false;
			boolean priceWitoutVatfound = false;
			
			for (String sProduct: listStringSplit) {
				if (sProduct.endsWith(".html")&& !productFound) {
					this.productPrice.setName(sProduct);
					productFound=true;
				}
				if (productFound &&(sProduct.contains("cena:"))) {
					String[] listStringSplitPrice = sProduct.split("span");
					for (String sPrice: listStringSplitPrice) {
						if((sPrice!=null)&&sPrice.contains("Kč")) {
							if (!priceVATfound) {
								priceVATfound=true;
								this.productPrice.setPriceVAT(sPrice.replaceAll("[<u>/]", ""));
								System.out.println("cena nalezena");
							} else {
								priceWitoutVatfound=true;
								this.productPrice.setPriceWithoutVAT(sPrice.replaceAll("[<u>/]", ""));
								break;
							}
						}
					}		
				}
				if (productFound && priceVATfound && priceWitoutVatfound) {
					break;
				}
			} 
			System.out.println("Produkt:"+this.productPrice.getName());
			
		
		} catch (UnirestException e) {
			System.out.println("Problém s načtenením stránky");
			this.productPrice.setName("Problém s načtenením stránky:"+e.getLocalizedMessage());
			this.productPrice.setPriceVAT("?????");
			this.productPrice.setPriceWithoutVAT("??????");
			e.printStackTrace();
		}
		System.out.println("konec - implementace service");
		return this.productPrice;
	}

}
