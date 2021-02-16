package de.fleigm.transitrouter.feeds.evaluation;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.vividsolutions.jts.geom.LineString;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.TransitFeedService;
import de.fleigm.transitrouter.http.pagination.Page;
import de.fleigm.transitrouter.http.pagination.Paged;
import de.fleigm.transitrouter.http.sort.SortQuery;
import de.fleigm.transitrouter.http.views.View;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("feeds/{id}/trips")
public class EvaluationTripController {

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  TransitFeedService transitFeedService;

  @Inject
  ReportService reportService;

  @GET
  @Path("{tripId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response show(@PathParam("id") UUID id, @PathParam("tripId") String tripId) {

    return generatedFeedRepository.find(id)
        .filter(GeneratedFeed::hasFinished)
        .map(info -> {
          TransitFeed originalFeed = transitFeedService.get(info.getOriginalFeed().getPath());
          TransitFeed generatedFeed = transitFeedService.get(info.getFeed().getPath());

          Trip trip = generatedFeed.internal().trips.get(tripId);
          Route route = generatedFeed.getRouteForTrip(tripId);
          List<Stop> stops = originalFeed.getOrderedStopsForTrip(tripId);
          LineString originalShape = originalFeed.internal().getTripGeometry(tripId);
          LineString generatedShape = generatedFeed.internal().getTripGeometry(tripId);

          View view = new View()
              .add("trip", trip)
              .add("route", route)
              .add("stops", stops)
              .add("originalShape", originalShape)
              .add("generatedShape", generatedShape);

          return Response.ok(view);
        })
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();


  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index(@PathParam("id") UUID id,
                        @Context UriInfo uriInfo,
                        @BeanParam Paged paged,
                        @QueryParam("search") @DefaultValue("") String search,
                        @QueryParam("sort") @DefaultValue("") String sort) {

    GeneratedFeed info = generatedFeedRepository.findOrFail(id);

    if (info.getStatus() != Status.FINISHED) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Evaluation evaluation = info.getExtension(Evaluation.class).get();
    Report report = reportService.get(evaluation.getReport());
    Stream<Report.Entry> entryQueryStream = report.entries().stream();

    if (!search.isBlank()) {
      entryQueryStream = entryQueryStream.filter(entry -> entry.tripId.contains(search));
    }

    if (!sort.isBlank()) {
      Comparator<Report.Entry> comparator = createComparator(SortQuery.parse(sort));
      if (comparator != null) {
        if (SortQuery.parse(sort).order() == SortQuery.SortOrder.DESC) {
          comparator = comparator.reversed();
        }
        entryQueryStream = entryQueryStream.sorted(comparator);
      }
    }

    TransitFeed transitFeed = transitFeedService.get(info.getFeed().getPath());

    List<Report.Entry> entries = entryQueryStream.collect(Collectors.toList());

    List<View> views = entries.stream()
        .skip(paged.getOffset())
        .limit(paged.getLimit())
        .map(entry -> new View()
            .add("tripId", entry.tripId())
            .add("an", entry.an())
            .add("al", entry.al())
            .add("avgFd", entry.avgFd())
            .add("route", transitFeed.getRouteForTrip(entry.tripId()).route_short_name))
        .collect(Collectors.toList());

    var page = Page.<View>builder()
        .data(views)
        .currentPage(paged.getPage())
        .perPage(paged.getLimit())
        .total(entries.size())
        .uri(uriInfo.getAbsolutePath())
        .build();

    return Response.ok(page).build();

  }


  private Comparator<Report.Entry> createComparator(SortQuery sortQuery) {
    Comparator<Report.Entry> comparator = null;
    switch (sortQuery.attribute()) {
      case "an":
        comparator = Comparator.comparingDouble(Report.Entry::an);
        break;
      case "al":
        comparator = Comparator.comparingDouble(Report.Entry::al);
        break;
      case "avgFd":
        comparator = Comparator.comparingDouble(Report.Entry::avgFd);
        break;
    }
    return comparator;
  }
}