package com.itc.service.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.itc.service.dao.CustomerDao;
import com.itc.service.model.Customer;
import com.itc.service.util.EncryptionDecryptionAES;
import com.itc.service.util.IPAddressUtil;
import com.itc.service.util.VerifyAccount;

@Component
@Path("/customer")
public class CustomerRestController {

	@Autowired
	CustomerDao customerDao;

	@GET
	@Path("/getip")
	public Response sayHello(@Context HttpServletRequest request) {
		String ipAddress = IPAddressUtil.getClientIp(request);
		String result = "Your Ip Is: " + ipAddress + " and domain name is: " + System.getenv("USERDOMAIN");
		return Response.status(200).entity(result).build();
	}

	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerCustomer(String jsonString, @Context HttpServletRequest request) {
		JsonObject jsonObject = new JsonObject();
		if (jsonString != null && !jsonString.isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				Customer customer = mapper.readValue(jsonString, Customer.class);
				if (customer.getEmailId() != null && !customer.getEmailId().isEmpty()
						&& customer.getOrganizationName() != null && !customer.getOrganizationName().isEmpty()) {
					boolean isPresent = customerDao.checkIfCustomerIsAlreadyPresentForTheEmail(customer.getEmailId());
					if (!isPresent) {
						String ipAddress = IPAddressUtil.getClientIp(request);
						String activationKey = EncryptionDecryptionAES
								.encrypt(customer.getEmailId() + "" + customer.getOrganizationName());

						if (ipAddress != null && !ipAddress.isEmpty() && activationKey != null
								&& !activationKey.isEmpty()) {
							customer.setIpAddress(ipAddress);
							customer.setActivationKey(activationKey);

							synchronized (customer) {
								boolean success = customerDao.createAndStoreCustomer(customer);
								if (success) {
									jsonObject.addProperty("activation_key", activationKey);
								} else {
									jsonObject.addProperty("error", "Some Error Occurred, Please try again later.");
								}
							}

						} else {
							jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
						}
					} else {
						jsonObject.addProperty("error", "You have already registered, please try with other email id.");
					}
				} else {
					jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
				}
			} catch (JsonParseException e) {
				jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
			} catch (JsonMappingException e) {
				jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
			} catch (IOException e) {
				jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
			}
		} else {
			jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}

	@POST
	@Path("/verify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response verifyAccount(String jsonString) {
		JsonObject jsonObject = new JsonObject();
		if (jsonString != null && !jsonString.isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				VerifyAccount verifyAccount = mapper.readValue(jsonString, VerifyAccount.class);
				if (verifyAccount.getEmailId() != null && !verifyAccount.getEmailId().isEmpty()
						&& verifyAccount.getActivationKey() != null && !verifyAccount.getActivationKey().isEmpty()) {
					boolean isAlreadyValidated = customerDao.checkIfCustomerIsAlreadyValidated(
							verifyAccount.getEmailId(), verifyAccount.getActivationKey());
					if (!isAlreadyValidated) {
						boolean validationSuccess = customerDao.validateCustomer(verifyAccount.getEmailId(),
								verifyAccount.getActivationKey());
						if (validationSuccess) {
							jsonObject.addProperty("success",
									"Account Verified and you have subscribed our service for 30 days.");
						} else {
							jsonObject.addProperty("error",
									"Please enter your registered email id and correct activation key to verify your account.");
						}
					} else {
						jsonObject.addProperty("success", "Your account is already verified.");
					}
				} else {
					jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
				}
			} catch (Exception e) {
				jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
			}
		} else {
			jsonObject.addProperty("error", "Problem in sent JSON, Please correct it and try again.");
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
}