/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.ruhrgebiet.analysis;

import org.apache.log4j.Logger;
import org.matsim.analysis.AgentAnalysisFilter;
import org.matsim.analysis.AgentFilter;
import org.matsim.analysis.MatsimAnalysis;
import org.matsim.analysis.TripFilter;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RunPersonTripAnalysisRuhr {
	private static final Logger log = Logger.getLogger(RunPersonTripAnalysisRuhr.class);
			
	public static void main(String[] args) throws IOException {
			
		String runDirectory = null;
		String runId = null;
		String runDirectoryToCompareWith = null;
		String runIdToCompareWith = null;
		String scenarioCRS = null;	
		String shapeFileZones = null;
		String zonesCRS = null;
		String zoneFile = null;
		String zoneId = null;
		String homeActivityPrefix = null;
		int scalingFactor;
		String modesString = null;
		String analysisOutputDirectory = null;
				
		if (args.length > 0) {
			throw new RuntimeException();
			
		} else {

			runDirectory = "C:\\Users\\Janek\\Desktop\\nemo_drt_output_highPrice";
			runId = "smartCity";

			runDirectoryToCompareWith = "C:\\Users\\Janek\\repos\\runs-svn\\nemo\\baseCaseCalibration2\\baseCase_021/output/";
			runIdToCompareWith = "baseCase_021";

			scenarioCRS = "EPSG:25832";

			shapeFileZones = "C:/Users/Janek/repos/shared-svn/projects/nemo_mercator/data/original_files/shapeFiles/plzBasedPopulation/plz-gebiete_Ruhrgebiet/plz-gebiete_Ruhrgebiet.shp";
			zonesCRS = "EPSG:25832";
			zoneId = "plz";

			zoneFile = "C:/Users/Janek/repos/shared-svn/projects/nemo_mercator/data/original_files/shapeFiles/shapeFile_Ruhrgebiet/ruhrgebiet_boundary.shp";

			homeActivityPrefix = "home";
			scalingFactor = 100;

			modesString = TransportMode.car + "," + TransportMode.pt + "," + TransportMode.bike + "," + TransportMode.walk + "," + TransportMode.ride + "," + TransportMode.drt;

			analysisOutputDirectory = "./scenarios/outputXXX/";
		}
		
		Scenario scenario1 = loadScenario(runDirectory, runId);
		Scenario scenario0 = loadScenario(runDirectoryToCompareWith, runIdToCompareWith);
		
		List<AgentFilter> agentFilters = new ArrayList<>();
		
		AgentAnalysisFilter filter1 = new AgentAnalysisFilter("all-agents");
		filter1.preProcess(scenario1);
		agentFilters.add(filter1);
		
		AgentAnalysisFilter filter2 = new AgentAnalysisFilter("ruhrgebiet-agents");
		filter2.setZoneFile(zoneFile);
		filter2.setRelevantActivityType(homeActivityPrefix);
		filter2.preProcess(scenario1);
		agentFilters.add(filter2);

		List<String> modes = new ArrayList<>();
		for (String mode : modesString.split(",")) {
			modes.add(mode);
		}

		MatsimAnalysis analysis = new MatsimAnalysis();
		analysis.setScenario1(scenario1);
		analysis.setScenario0(scenario0);

		List<TripFilter> tripFilters = new ArrayList<>();
		TripFilter tripFilter = new TripFilter() {
			@Override
			public boolean considerTrip(Coord origin, Coord destination) {
				return true;
			}

			@Override
			public String toFileName() {
				return "-all-";
			}
		};
		tripFilters.add(tripFilter);
		analysis.setTripFilters(tripFilters);
		analysis.setAgentFilters(agentFilters);

		analysis.setScenarioCRS(scenarioCRS);
		analysis.setZoneInformation(shapeFileZones, zonesCRS, zoneId);

		analysis.setModes(modes);
		analysis.setVisualizationScriptInputDirectory(null);
		analysis.setHomeActivityPrefix(homeActivityPrefix);
		analysis.setScalingFactor(scalingFactor);
		
		analysis.setAnalysisOutputDirectory(analysisOutputDirectory);
		
		analysis.run();
	}
	
	private static Scenario loadScenario(String runDirectory, String runId) {
		log.info("Loading scenario...");
		
		if (runDirectory == null || runDirectory.equals("") || runDirectory.equals("null")) {
			return null;	
		}
		
		if (!runDirectory.endsWith("/")) runDirectory = runDirectory + "/";

		String configFile = runDirectory + runId + ".output_config.xml";
		String networkFile = runId + ".output_network.xml.gz";
		String populationFile = runId + ".output_plans.xml.gz";

		Config config = ConfigUtils.loadConfig(configFile);

		config.controler().setOutputDirectory(runDirectory);
		config.plans().setInputFile(populationFile);
		config.network().setInputFile(networkFile);
		config.vehicles().setVehiclesFile(null);
		config.transit().setTransitScheduleFile(null);
		config.transit().setVehiclesFile(null);
		
		return ScenarioUtils.loadScenario(config);
	}

}
