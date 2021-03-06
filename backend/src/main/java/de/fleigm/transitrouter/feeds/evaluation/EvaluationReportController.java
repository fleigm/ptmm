package de.fleigm.transitrouter.feeds.evaluation;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.TransitFeedService;
import de.fleigm.transitrouter.gtfs.Type;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

@Path("feeds/{id}/report")
public class EvaluationReportController {
  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  ReportService reportService;

  @Inject
  TransitFeedService transitFeedService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index(@PathParam("id") UUID id) {
    Optional<GeneratedFeed> info = generatedFeedRepository.find(id);

    if (info.isEmpty() || info.get().getStatus() != Status.FINISHED) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    TransitFeed transitFeed = transitFeedService.get(info.get().getFeed().getPath());

    return info.flatMap(i -> i.getExtension(Evaluation.class))
        .map(Evaluation::getReport)
        .map(reportService::get)
        .map(report -> {
          report.entries().forEach(entry -> entry.type = Type.create(transitFeed.getRouteForTrip(entry.tripId).route_type).value());
          return report;
        })
        .map(Response::ok)
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();
  }

}
