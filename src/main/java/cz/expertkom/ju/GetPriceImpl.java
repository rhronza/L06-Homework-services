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
			/* načtení stránky do proměnné typu String */
			String stringDownloadedWebPage = Unirest.get(WEB_PAGE_DOWNLOAD).asString().getBody();
			/* split podle znaku apostrofu: */
			String[] listStringSplit = stringDownloadedWebPage.split("'");
			
			boolean productFound = false;
			boolean priceVATfound = false;
			boolean priceWitoutVatfound = false;
			
			/* iterace pole vzniklého splitem */ 
			for (String sProduct: listStringSplit) {
				/* pokud řetez končí ".html" - jedná se o název výrobku*/ 
				if (sProduct.endsWith(".html")&& !productFound) {
					this.productPrice.setName(sProduct);
					productFound=true;
				}
				/* jestliže následující řetez obsahuje "cena:", pak se provede splitování podle "span" a odstraněním znaků "<u>/" vznikne cena v Kč */   
				if (productFound &&(sProduct.contains("cena:"))) {
					String[] listStringSplitPrice = sProduct.split("span");
					for (String sPrice: listStringSplitPrice) {
						if((sPrice!=null)&&sPrice.contains("Kč")) {
							if (!priceVATfound) {
								priceVATfound=true;
								this.productPrice.setPriceWithVAT(sPrice.replaceAll("[<u>/]", ""));
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
		
		} catch (UnirestException e) {
			System.out.println("Problém s načtenením stránky");
			this.productPrice.setName("Problém s načtenením stránky:"+e.getLocalizedMessage());
			this.productPrice.setPriceWithVAT("?????");
			this.productPrice.setPriceWithoutVAT("??????");
			e.printStackTrace();
		}
		System.out.println(productPrice);
		return this.productPrice;
	}

}
