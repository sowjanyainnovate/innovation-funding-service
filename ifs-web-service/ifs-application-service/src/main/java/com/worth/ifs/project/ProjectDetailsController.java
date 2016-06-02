package com.worth.ifs.project;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.service.ApplicationService;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.ProjectService;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.model.OrganisationDetailsModelPopulator;
import com.worth.ifs.project.form.ProjectManagerForm;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.user.resource.ProcessRoleResource;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.ProcessRoleService;
import com.worth.ifs.user.service.UserService;

import static java.util.Arrays.asList;

/**
 * This controller will handle all requests that are related to project details.
 */
@Controller
@RequestMapping("/project")
public class ProjectDetailsController {

    private static final Log LOG = LogFactory.getLog(ProjectDetailsController.class);

	@Autowired
    private ProjectService projectService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CompetitionService competitionService;
    
    @Autowired
    private OrganisationDetailsModelPopulator organisationDetailsModelPopulator;
    
    @Autowired
    private UserAuthenticationService userAuthenticationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProcessRoleService processRoleService;
    
    @RequestMapping(value = "/{projectId}/details", method = RequestMethod.GET)
    public String projectDetail(Model model, @PathVariable("projectId") final Long projectId, HttpServletRequest request) {
        ProjectResource projectResource = projectService.getById(projectId);
        ApplicationResource applicationResource = applicationService.getById(projectId);
        CompetitionResource competitionResource = competitionService.getById(applicationResource.getCompetition());
        UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        
        organisationDetailsModelPopulator.populateModel(model, projectId);
        
        model.addAttribute("project", projectResource);
        model.addAttribute("currentUser", user);
        model.addAttribute("currentOrganisation", user.getOrganisations().get(0));
        model.addAttribute("app", applicationResource);
        model.addAttribute("competition", competitionResource);
        return "project/detail";
    }
    
    @RequestMapping(value = "/{projectId}/details/project-manager", method = RequestMethod.GET)
    public String projectManager(Model model, @PathVariable("projectId") final Long projectId) {
    	ProjectManagerForm form = populateOriginalProjectManagerForm(projectId);
        
        ApplicationResource applicationResource = applicationService.getById(projectId);

        populateProjectManagerModel(model, projectId, form, applicationResource);
    	
        return "project/project-manager";
    }

    @RequestMapping(value = "/{projectId}/details/project-manager", method = RequestMethod.POST)
    public String updateProjectManager(Model model, @PathVariable("projectId") final Long projectId, @ModelAttribute ProjectManagerForm form, BindingResult bindingResult) {
        
        ApplicationResource applicationResource = applicationService.getById(projectId);
        
        if(bindingResult.hasErrors()) {
        	populateProjectManagerModel(model, projectId, form, applicationResource);
            return "project/project-manager";
        }
        
        if(!userIsInLeadPartnerOrganisation(applicationResource, form.getProjectManager())) {
        	populateProjectManagerModel(model, projectId, form, applicationResource);
            return "project/project-manager";
        }
        
        projectService.updateProjectManager(projectId, form.getProjectManager());
    	
        return "redirect:/project/" + projectId + "/details";
    }

	private ProjectManagerForm populateOriginalProjectManagerForm(final Long projectId) {
		ProjectResource projectResource = projectService.getById(projectId);
    	Future<ProcessRoleResource> processRoleResource;
    	if(projectResource.getProjectManager() != null) {
    		processRoleResource = processRoleService.getById(projectResource.getProjectManager());
    	} else {
    		processRoleResource = null;
    	}
    	
        ProjectManagerForm form = new ProjectManagerForm();
        if(processRoleResource != null) {
        	try {
				form.setProjectManager(processRoleResource.get().getUser());
			} catch (InterruptedException | ExecutionException e) {
				LOG.error("unable to get process role", e);
			}
        }
		return form;
	}
    
	private void populateProjectManagerModel(Model model, final Long projectId, ProjectManagerForm form,
			ApplicationResource applicationResource) {
		ProjectResource projectResource = projectService.getById(projectId);
		
		ProcessRoleResource lead = userService.getLeadApplicantProcessRoleOrNull(applicationResource);
		List<ProcessRoleResource> leadPartnerUsers = userService.getLeadPartnerOrganisationProcessRoles(applicationResource);
		List<ProcessRoleResource> leadPartnerUsersExcludingLead = leadPartnerUsers.stream()
				.filter(prr -> !lead.getUser().equals(prr.getUser()))
				.collect(Collectors.toList());
		
		List<ProcessRoleResource> allUsers = Stream.of(asList(lead), leadPartnerUsersExcludingLead)
				.flatMap(x -> x.stream())
				.collect(Collectors.toList());

		model.addAttribute("allUsers", allUsers);
		model.addAttribute("project", projectResource);
		model.addAttribute("app", applicationResource);
		model.addAttribute("form", form);
	}

	private boolean userIsInLeadPartnerOrganisation(ApplicationResource applicationResource, Long projectManager) {
		
		if(projectManager == null) {
			return false;
		}
        List<ProcessRoleResource> leadPartnerUsers = userService.getLeadPartnerOrganisationProcessRoles(applicationResource);

        return leadPartnerUsers.stream().anyMatch(prr -> projectManager.equals(prr.getUser()));
	}
}
