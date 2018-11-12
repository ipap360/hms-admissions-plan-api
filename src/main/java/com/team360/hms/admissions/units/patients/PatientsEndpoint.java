package com.team360.hms.admissions.units.patients;

import com.team360.hms.admissions.units.WebUtl;
import com.team360.hms.admissions.web.filters.Secured;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Log4j2
@Path("patients")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Secured
public class PatientsEndpoint {

    @Context
    ContainerRequestContext crc;

    @GET
    public Response get() {
//        Stream<DBEntity> s = .stream().map((map) -> new Patient().load(map));
        return Response.ok().entity(new PatientDao().list()).build();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        Patient patient = new Patient();
        WebUtl.db(crc).read(patient.setId(id));
        PatientForm form = new PatientForm();
        return Response.ok().entity(form.load(patient)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(@PathParam("id") Integer id, PatientForm form) {
        form.setId(id);
        form.validate();
        Patient patient = new Patient();
        WebUtl.db(crc).upsert(patient.load(form));
        return Response.ok().entity(form.load(patient)).build();
    }


    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        Patient patient = new Patient();
        patient.setId(id);
        WebUtl.db(crc).delete(patient);
        return Response.ok().build();
    }

}
