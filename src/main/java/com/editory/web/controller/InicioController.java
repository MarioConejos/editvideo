package com.editory.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.editory.dropbox.DropboxManager;
import com.editory.web.mail.EmailSender;

@Controller
public class InicioController {

	
	
	// Tiene 29 caracteres
	private static String CTE_HOME = "https://www.dropbox.com/home/";
	
	
	
	@RequestMapping("/")
	public String inicio(Model model) {

		Random random = new Random(System.currentTimeMillis());
		model.addAttribute("userId", random.nextLong());

		//redirect:/TestStuff.html
		return "redirect:/index.html";
	}
	
	
	
	@RequestMapping("/enviarCorreo")
	public String enviarCorreo(@RequestParam String Nombre,
			@RequestParam String Email,
			@RequestParam String Telefono,
			@RequestParam String TextoInicial,
			@RequestParam String TextoFinal,
			@RequestParam String Cancion,
			@RequestParam String Parte,
			@RequestParam String Persona,
			@RequestParam String Historia,
			@RequestParam String precio,
			@RequestParam String carpeta,
			@RequestParam String ruta,
			Model model) {
		
		HashMap<String, String> camposCorreo = new HashMap<>();
		camposCorreo.put("TIPO DE VIDEO ", "Recuerdo");
		camposCorreo.put("Nombre ", Nombre);
		camposCorreo.put("mail",Email);
		camposCorreo.put("Telefono ",Telefono);
		camposCorreo.put("Texto Inicial ",TextoInicial);
		camposCorreo.put("Texto Final ",TextoFinal);
		camposCorreo.put("Cancion ",Cancion);
		camposCorreo.put("Persona con más importancia ",Persona);
		camposCorreo.put("Parte favorita del video ",Parte);
		camposCorreo.put("Historia ", Historia);
		camposCorreo.put("Precio ", precio+"€");
		camposCorreo.put("Carpeta ", CTE_HOME+carpeta);
		
		EmailSender mensajero = new EmailSender(camposCorreo);
		mensajero.enviar();
		
		HashMap<String, String> correoCliente = new HashMap<>();
		correoCliente.put("nombre", Nombre);
		correoCliente.put("precio", precio);
		correoCliente.put("mail", Email);
		EmailSender cliente = new EmailSender(correoCliente);
		cliente.enviarCliente();
		
		
		
		
		try{
		    PrintWriter writer = new PrintWriter("Instrucciones.txt", "UTF-8");
		    writer.println("Video a editar de tipo EDICIÓN");
		    writer.println("El texto inicial es:  "+TextoInicial);
		    writer.println("El texto final es:  "+TextoFinal);
		    writer.println("La canción elegida es:   "+Cancion);
		    writer.println("La persona con más importancia es:   "+Persona);
		    writer.println("La parte favorita del video es:   "+Parte);
		    writer.println("La historia del video es:   "+Historia);
		    
		    writer.close();
		} catch (IOException e) {
		   System.err.println("No se ha podido crear el fichero con las instrucciones");
		}
		
		DbxClientV2 dbxClient;
		DbxRequestConfig requestConfig;
		requestConfig = new DbxRequestConfig("la1sr3tp42tnts9");
		dbxClient = new DbxClientV2(requestConfig, "a5tfLg53tQAAAAAAAAAAC1CcwT8aT8Rm9CiEcmrFQObV9X5odx_51BqH2POsFduU");
		
		File dato = new File("./Instrucciones.txt");
		
		String dropboxPath = "/"+ruta+"/"+"Instrucciones.txt";
		
		DropboxManager gestorDropbox = new DropboxManager();
		gestorDropbox.uploadFile(dbxClient, dato, dropboxPath);
		
		return "redirect:http://editory.wetransfer.com";
		
		
		
		//TODO Success html
		
		
	}
	
	
	////enviarCorreoRecopilacion
	
	
	@RequestMapping("/enviarCorreoRecopilacion")
	public String enviarCorreoRecopilacion(@RequestParam String Nombre,
			@RequestParam String Email,
			@RequestParam String Telefono,
			@RequestParam String TextoInicial,
			@RequestParam String precio,
			@RequestParam String carpeta,
			@RequestParam String ruta,
			Model model) {
		
		HashMap<String, String> camposCorreo = new HashMap<>();
		camposCorreo.put("TIPO DE VIDEO", "Recuerdo");
		camposCorreo.put("Nombre", Nombre);
		camposCorreo.put("mail",Email);
		camposCorreo.put("Telefono",Telefono);
		camposCorreo.put("Texto Inicial",TextoInicial);
		camposCorreo.put("Precio", precio+"€");
		camposCorreo.put("Carpeta", CTE_HOME+carpeta);
		
		EmailSender mensajero = new EmailSender(camposCorreo);
		mensajero.enviar();
		
		HashMap<String, String> correoCliente = new HashMap<>();
		correoCliente.put("nombre", Nombre);
		correoCliente.put("precio", precio);
		correoCliente.put("mail", Email);
		EmailSender cliente = new EmailSender(correoCliente);
		cliente.enviarCliente();
		
		
		
		
				
		try{
		    PrintWriter writer = new PrintWriter("Instrucciones.txt", "UTF-8");
		    writer.println("Video a editar de tipo RECOPILACIÓN");
		    writer.println("El texto inicial es:  "+TextoInicial);
		    writer.close();
		} catch (IOException e) {
		   // do something
		}
		
		DbxClientV2 dbxClient;
		DbxRequestConfig requestConfig;
		requestConfig = new DbxRequestConfig("la1sr3tp42tnts9");
		dbxClient = new DbxClientV2(requestConfig, "a5tfLg53tQAAAAAAAAAAC1CcwT8aT8Rm9CiEcmrFQObV9X5odx_51BqH2POsFduU");
		
		File dato = new File("./Instrucciones.txt");
		
		String dropboxPath = "/"+ruta+"/"+"Instrucciones.txt";
		
		DropboxManager gestorDropbox = new DropboxManager();
		gestorDropbox.uploadFile(dbxClient, dato, dropboxPath);
		
		
		
		
		return "redirect:http://editory.wetransfer.com";
		
	}
	
	
	
	
	
	
	@RequestMapping("/enviarCorreoRegalo")
	public String enviarCorreoRegalo(@RequestParam String Nombre,
			@RequestParam String Email,
			@RequestParam String Telefono,@RequestParam String TextoInicial,
			@RequestParam String TextoFinal,
			@RequestParam String Cancion,
			@RequestParam String Presupuesto,
			Model model) {
		
		HashMap<String, String> camposCorreo = new HashMap<>();

		camposCorreo.put("TIPO DE VIDEO ", "Regalo");
		camposCorreo.put("Nombre ", Nombre);
		camposCorreo.put("mail",Email);
		camposCorreo.put("Teléfono ",Telefono);
		camposCorreo.put("Inicial ",TextoInicial);
		camposCorreo.put("Final ",TextoFinal);
		camposCorreo.put("Canción ",Cancion);
		camposCorreo.put("Presupuesto ",Presupuesto+"€");

		
		EmailSender mensajero = new EmailSender(camposCorreo);
		mensajero.enviar();
		
		
		HashMap<String, String> correoCliente = new HashMap<>();
		correoCliente.put("nombre", Nombre);
		correoCliente.put("precio", Presupuesto);
		correoCliente.put("mail", Email);
		EmailSender cliente = new EmailSender(correoCliente);
		cliente.enviarCliente();

		
		
		
		
		return "redirect:/index.html";
		
	}
	
	
	
	@RequestMapping("/recuerdo")
	public String recuerdo(Model model){
		return "recuerdo";
	}
	
	
	
	
	@RequestMapping("/subirArchivos")
	public String subirArchivos(Model model, @RequestParam("files") List<MultipartFile> files,
            RedirectAttributes redirectAttributes, @RequestParam("duracion") String duracion){
		
//		DbxClientV2 dbxClient;
//		DbxRequestConfig requestConfig;
//		requestConfig = new DbxRequestConfig("la1sr3tp42tnts9");
//		dbxClient = new DbxClientV2(requestConfig, "a5tfLg53tQAAAAAAAAAAC1CcwT8aT8Rm9CiEcmrFQObV9X5odx_51BqH2POsFduU");
//	
//		String carpeta = new Date().toString();
//		carpeta = carpeta + Integer.toString(new Random().nextInt()	);	
//		for(MultipartFile dato : files){
//			
//				
//			String extension = dato.getOriginalFilename().split("\\.")[1];
//			String nombre = dato.getOriginalFilename().split("\\.")[0];
//			
//			
//			
//			String dropboxPath = "/"+carpeta+"/"+nombre+"."+extension;
//			
//			DropboxManager gestorDropbox = new DropboxManager();
//			gestorDropbox.chunkedUploadFile(dbxClient, dato, dropboxPath);
//			
//		}
		
		int precioBarato = Integer.parseInt(duracion);
		int precioMedio=0, precioCaro=0;
		switch(precioBarato){
		case 75: precioMedio = 90; precioCaro = 190; break;
		case 100: precioMedio= 160; precioCaro = 250; break;
		case 200: precioMedio = 300; precioCaro = 400; break;
		}
		
		//String link = DropboxManager.linkCarpeta(carpeta).substring(29);
		
		String link = "";
		String carpeta = "";
		
		
		model.addAttribute("ruta", carpeta);
		model.addAttribute("carpeta",link);
		model.addAttribute("precioBarato", precioBarato);
		model.addAttribute("precioMedio", precioMedio);
		model.addAttribute("precioCaro", precioCaro);

		
		return "confirmaRecuerdo";
	}
	
	///confirmarRegalo
	@RequestMapping("/confirmarRegalo")
	public String confirmarRegalo(Model model,  @RequestParam("videos") String videos, @RequestParam("duracion") String duracion,@RequestParam("fotos") String fotos){
	
		int precioTotal = 70 + Integer.parseInt(videos)+ Integer.parseInt(duracion)+ Integer.parseInt(fotos);
		model.addAttribute("precioTotal", precioTotal);
		return "confirmaRegalo";
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	@RequestMapping("/continuarRecopilacion")
	public String continuarRecopilacion(Model model, @RequestParam("precioBarato") String precioBarato,
			@RequestParam("precioMedio") String precioMedio, @RequestParam("precioCaro") String precioCaro,
			@RequestParam("carpeta") String carpeta, @RequestParam("ruta") String ruta,RedirectAttributes redirectAttributes) {	
		
		System.out.println("Comienzo del controlador de continuarRecopilacion");
		
		model.addAttribute("precioBarato", precioBarato);
		model.addAttribute("precioMedio", precioMedio);
		model.addAttribute("precioCaro", precioCaro);
		model.addAttribute("carpeta", carpeta);
		model.addAttribute("ruta", ruta);
		
		System.out.println("Final del controlador de continuarRecopilacion");
		return "salidaRecopilacion";
	
	}
	
	///continuarEdicion
	@RequestMapping("/continuarEdicion")
	public String continuarEdicion(Model model, @RequestParam("precioBarato") String precioBarato,
			@RequestParam("precioMedio") String precioMedio, @RequestParam("precioCaro") String precioCaro,
			@RequestParam("carpeta") String carpeta, @RequestParam("ruta") String ruta) {	
		
		model.addAttribute("precioBarato", precioBarato);
		model.addAttribute("precioMedio", precioMedio);
		model.addAttribute("precioCaro", precioCaro);
		model.addAttribute("carpeta", carpeta);
		model.addAttribute("ruta", ruta);
		return "salidaEdicion";
	
	}
	
	//continuarEdicionPremium
	@RequestMapping("/continuarEdicionPremium")
	public String continuarEdicionPremium(Model model, @RequestParam("precioBarato") String precioBarato,
			@RequestParam("precioMedio") String precioMedio, @RequestParam("precioCaro") String precioCaro,
			@RequestParam("carpeta") String carpeta, @RequestParam("ruta") String ruta) {	
		
		model.addAttribute("precioBarato", precioBarato);
		model.addAttribute("precioMedio", precioMedio);
		model.addAttribute("precioCaro", precioCaro);
		model.addAttribute("carpeta", carpeta);
		model.addAttribute("ruta", ruta);
		return "salidaEdicionPremium";
	
	}
	
}
