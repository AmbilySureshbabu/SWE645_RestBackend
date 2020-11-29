package com.swe.assignment;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.swe.assignment.bean.StudentBean;
import com.swe.assignment.dao.impl.StudentKafkaImpl;

@Path("/survey")
public class SurveyKafkaResource {


	public SurveyKafkaResource() {

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllStudentsSurveyForm() {
		try {
			List<String> obj = StudentKafkaImpl.getInstance().readStudentIds();
			return Response.status(200).entity(obj).build();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return Response.status(500).build();
	}

	@GET
	@Path("{studentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStudentSurveyForm(@PathParam("studentId") String id) {
		// initialize return studentBean to null
		StudentBean studentBean = null;
		try {
			// if queryString is available
			if (null != id) {
				// get the database instance object and read the student based on the
				// id(converting to int as we get it as string and in database we store as
				// integer)
				studentBean = StudentKafkaImpl.getInstance().readStudent(Integer.parseInt(id));
				return Response.status(200).entity(studentBean).build();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return Response.status(500).build();
		}
		return Response.status(400).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSurveyRecord(StudentBean studentBean) {
		try {

			StudentKafkaImpl impl = StudentKafkaImpl.getInstance();
			// insert the student into the kafka, if any reason student insert fails it
			impl.saveToDatabase(studentBean);
		} catch (Exception e) {
			// if the student insertion fails send to the home page with reason for the
			// error
			System.out.println("exception occurred during insertion " + e.getMessage());
			return Response.status(500).build();
		}
		return Response.status(200).entity(studentBean).build();
	}


}
