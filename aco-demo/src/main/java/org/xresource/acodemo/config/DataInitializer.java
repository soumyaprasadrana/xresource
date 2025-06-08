package org.xresource.acodemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.xresource.acodemo.repositories.*;
import org.xresource.acodemo.entity.*;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CountryRepository countryRepo;
    @Autowired
    private RegionRepository regionRepo;
    @Autowired
    private CityRepository cityRepo;
    @Autowired
    private AddressRepository addressRepo;
    @Autowired
    private OrganizationRepository organizationRepo;
    @Autowired
    private PersonRepository personRepo;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private ProjectMemberRepository projectMemberRepo;
    @Autowired
    private AssetRepository assetRepo;
    @Autowired
    private AssetLocationRepository assetLocationRepo;
    @Autowired
    private AssetUsageRepository assetUsageRepo;
    @Autowired
    private AssetMaintenanceRepository assetMaintenanceRepo;

    @Override
    public void run(String... args) throws Exception {
        // COUNTRY
        if (countryRepo.count() < 5000) {
            List<Country> countries = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                Country country = new Country();
                country.setName("Country " + i);
                country.setCode("C" + i);
                countries.add(country);
            }
            countryRepo.saveAll(countries);
        }

        // REGION
        if (regionRepo.count() < 5000) {
            List<Country> countries = countryRepo.findAll();
            List<Region> regions = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                Region region = new Region();
                region.setName("Region " + i);
                region.setCountry(countries.get(i % countries.size()));
                regions.add(region);
            }
            regionRepo.saveAll(regions);
        }

        // CITY
        if (cityRepo.count() < 5000) {
            List<Region> regions = regionRepo.findAll();
            List<City> cities = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                City city = new City();
                city.setName("City " + i);
                city.setRegion(regions.get(i % regions.size()));
                cities.add(city);
            }
            cityRepo.saveAll(cities);
        }

        // ADDRESS
        if (addressRepo.count() < 5000) {
            List<City> cities = cityRepo.findAll();
            List<Address> addresses = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                Address address = new Address();
                address.setStreet("Street " + i);
                address.setPostalCode("ZIP" + i);
                address.setType("Type" + (i % 3));
                address.setCity(cities.get(i % cities.size()));
                addresses.add(address);
            }
            addressRepo.saveAll(addresses);
        }

        // ORGANIZATION
        if (organizationRepo.count() < 5000) {
            List<Address> addresses = addressRepo.findAll();
            List<Organization> orgs = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                Organization org = new Organization();
                org.setName("Organization " + i);
                org.setType("Type" + (i % 5));
                org.setAddress(addresses.get(i % addresses.size()));
                orgs.add(org);
            }
            organizationRepo.saveAll(orgs);
        }

        // PERSON
        if (personRepo.count() < 5000) {
            List<Organization> orgs = organizationRepo.findAll();
            List<Address> addresses = addressRepo.findAll();
            List<Person> people = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                Person p = new Person();
                p.setName("Person " + i);
                p.setRole("Role" + (i % 4));
                p.setOrganization(orgs.get(i % orgs.size()));
                p.setAddress(addresses.get(i % addresses.size()));
                people.add(p);
            }
            // Set supervisors randomly
            for (Person p : people) {
                p.setSupervisor(people.get(new Random().nextInt(people.size())));
            }
            personRepo.saveAll(people);
        }

        // PROJECT
        if (projectRepo.count() < 5000) {
            List<Organization> orgs = organizationRepo.findAll();
            List<Project> projects = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                Project project = new Project();
                project.setTitle("Project " + i);
                project.setOrganization(orgs.get(i % orgs.size()));
                project.setStartDate(
                        Date.from(LocalDate.now().minusDays(i % 365).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                project.setEndDate(
                        Date.from(LocalDate.now().plusDays(i % 365).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                project.setStatus(i % 2 == 0 ? "ACTIVE" : "CLOSED");
                projects.add(project);
            }
            projectRepo.saveAll(projects);
        }

        // PROJECT MEMBER
        if (projectMemberRepo.count() < 5000) {
            List<Project> projects = projectRepo.findAll();
            List<Person> people = personRepo.findAll();
            List<ProjectMember> members = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                ProjectMember pm = new ProjectMember();
                pm.setProject(projects.get(i % projects.size()));
                pm.setPerson(people.get(i % people.size()));
                pm.setRole("MemberRole" + (i % 3));
                pm.setJoinedOn(
                        Date.from(LocalDate.now().minusDays(i % 200).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                members.add(pm);
            }
            projectMemberRepo.saveAll(members);
        }

        // ASSET
        if (assetRepo.count() < 5000) {
            List<Organization> orgs = organizationRepo.findAll();
            List<Asset> assets = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                Asset asset = new Asset();
                asset.setTag("TAG-" + i);
                asset.setDescription("Asset Description " + i);
                asset.setType("Type" + (i % 6));
                asset.setOwnerOrganization(orgs.get(i % orgs.size()));
                assets.add(asset);
            }
            assetRepo.saveAll(assets);
        }

        // ASSET LOCATION
        if (assetLocationRepo.count() < 5000) {
            List<Asset> assets = assetRepo.findAll();
            List<Address> addresses = addressRepo.findAll();
            List<AssetLocation> assetLocations = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                AssetLocation al = new AssetLocation();
                al.setAsset(assets.get(i % assets.size()));
                al.setAddress(addresses.get(i % addresses.size()));
                al.setFromDate(
                        Date.from(LocalDate.now().minusDays(i % 100).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                al.setToDate(
                        Date.from(LocalDate.now().plusDays(i % 100).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                assetLocations.add(al);
            }
            assetLocationRepo.saveAll(assetLocations);
        }

        // ASSET USAGE
        if (assetUsageRepo.count() < 5000) {
            List<Asset> assets = assetRepo.findAll();
            List<Project> projects = projectRepo.findAll();
            List<ProjectMember> members = projectMemberRepo.findAll();
            List<AssetUsage> usages = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                AssetUsage au = new AssetUsage();
                au.setAsset(assets.get(i % assets.size()));
                au.setProject(projects.get(i % projects.size()));
                au.setUsedByProjectMember(members.get(i % members.size()));
                au.setFromDate(
                        Date.from(LocalDate.now().minusDays(i % 60).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                au.setToDate(
                        Date.from(LocalDate.now().plusDays(i % 60).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                usages.add(au);
            }
            assetUsageRepo.saveAll(usages);
        }

        // ASSET MAINTENANCE
        if (assetMaintenanceRepo.count() < 5000) {
            List<Asset> assets = assetRepo.findAll();
            List<ProjectMember> members = projectMemberRepo.findAll();
            List<AssetMaintenance> maints = new ArrayList<>();
            for (int i = 1; i <= 5000; i++) {
                AssetMaintenance am = new AssetMaintenance();
                am.setAsset(assets.get(i % assets.size()));
                am.setPerformedByProjectMember(members.get(i % members.size()));
                am.setDate(
                        Date.from(LocalDate.now().minusDays(i % 365).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                am.setDetails("Maint details " + i);
                maints.add(am);
            }
            assetMaintenanceRepo.saveAll(maints);
        }
    }
}
