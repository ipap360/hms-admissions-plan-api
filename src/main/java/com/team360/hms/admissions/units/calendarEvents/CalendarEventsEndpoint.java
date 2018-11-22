package com.team360.hms.admissions.units.calendarEvents;

import com.team360.hms.admissions.units.WebUtl;
import com.team360.hms.admissions.web.filters.Secured;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Secured
@Log4j2
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Path("calendar-events")
public class CalendarEventsEndpoint {

    @Context
    ContainerRequestContext crc;

    @GET
    public Response get(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("patient") Integer patient) {

        if (from != null && to != null) {
            return Response.ok().entity((new CalendarEventDao().listByDate(from, to))).build();
        } else if (patient != null) {
            return Response.ok().entity((new CalendarEventDao().listByPatient(patient))).build();
        } else {
            return Response.ok().build();
        }
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("id") Integer id) {
        CalendarEvent event = new CalendarEvent();
        WebUtl.db(crc).read(event.setId(id));
        CalendarEventForm form = new CalendarEventForm();
        return Response.ok().entity(form.load(event, getOriginalEvent(event))).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upsert(@PathParam("id") Integer id, CalendarEventForm form) {
        form.setId(id);
        form.validate();
        CalendarEvent event = new CalendarEvent();
        WebUtl.db(crc).upsert(event.load(form));
        return Response.ok().entity(form.load(event, getOriginalEvent(event))).build();
    }

    @POST
    @Path("/{id}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Integer id) {
        CalendarEvent event = new CalendarEvent();
        event.setId(id);
        WebUtl.db(crc).delete(event);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/postpone")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postpone(@PathParam("id") Integer id, CalendarEventCopyForm form) {
        return Response.ok().entity(copyOrPostpone("POSTPONE", id, form)).build();
    }

    @POST
    @Path("/{id}/copy")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response copy(@PathParam("id") Integer id, CalendarEventCopyForm form) {
        return Response.ok().entity(copyOrPostpone("COPY", id, form)).build();
    }

    private CalendarEvent getOriginalEvent(CalendarEvent event) {
        CalendarEvent originalEvent = null;
        if (event.getPostponeId() != null && event.getPostponeId() != 0) {
            originalEvent = new CalendarEvent();
            WebUtl.db(crc).read(originalEvent.setId(event.getPostponeId()));
        }
        return originalEvent;
    }

    private CalendarEvent copyOrPostpone(String mode, Integer id, CalendarEventCopyForm form) {
        CalendarEvent event1 = new CalendarEvent();
        event1.setId(id);
        WebUtl.db(crc).read(event1);
        form.validate(event1);

        CalendarEvent event2 = new CalendarEvent();
        event2.setPatientId(event1.getPatientId());

        event2.setAdmissionDate(form.getDate());
        event2.setReleaseDate(form.getDate().plusDays(event1.getDuration()));
        event2.setNotes(form.getNotes());

        if ("postpone".equalsIgnoreCase(mode)) {
            event1.setIsPostponed(true);
            event2.setPostponeId(event1.getId());
        } else {
            event1.setIsCopied(true);
            event1.setIsCompleted(true);
        }

        WebUtl.db(crc).upsert(event1, event2);
        return event2;
    }

}
