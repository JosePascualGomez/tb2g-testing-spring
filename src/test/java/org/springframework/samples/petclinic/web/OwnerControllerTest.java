package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml"})
class OwnerControllerTest {
    @Autowired
    OwnerController ownerController;
    @Autowired
    ClinicService clinicService;

    MockMvc mockMvc;
    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();
    }

    @AfterEach
    void tearDown(){
        reset(clinicService);
    }

    @Test
    void testOwnerUpdateValid() throws Exception{
        mockMvc.perform(post("/owners/{ownerId}/edit",1)
                        .param("firstName", "jose")
                        .param("lastName", "gomez")
                        .param("address","carrera 34")
                        .param("city", "Duitama")
                        .param("telephone","3126547989"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/{ownerId}"));
    }
    @Test
    void testOwnerUpdateNotValid() throws Exception{
        mockMvc.perform(post("/owners/{ownerId}/edit",1)
                        .param("firstName", "jose")
                        .param("lastName", "gomez")
                        .param("address","carrera 34"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("owner"))
                .andExpect(model().attributeHasFieldErrors("owner","city"))
                .andExpect(model().attributeHasFieldErrors("owner","telephone"))
                .andExpect(view().name(OwnerController.VIEWS_OWNER_CREATE_OR_UPDATE_FORM));
    }

    @Test
    void testNewOwnerPostValid() throws Exception{
        mockMvc.perform(post("/owners/new")
                    .param("firstName", "jose")
                    .param("lastName", "gomez")
                    .param("address","carrera 34")
                    .param("city", "Duitama")
                    .param("telephone","3126547989"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testNewOwnerPostNotValid() throws Exception{
        mockMvc.perform(post("/owners/new")
                        .param("firstName", "jose")
                        .param("lastName", "gomez")
                        .param("city", "Duitama"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("owner"))
                .andExpect(model().attributeHasFieldErrors("owner","address"))
                .andExpect(model().attributeHasFieldErrors("owner","telephone"))
                .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void testReturnListOfOwners() throws Exception {
        given(clinicService.findOwnerByLastName(""))
                .willReturn(Lists.newArrayList(new Owner(), new Owner()));
        mockMvc.perform(get("/owners"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/ownersList"));
        then(clinicService).should().findOwnerByLastName(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualToIgnoringCase("");
    }

    @Test
    void testFindByNameNotFound() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        final String findListOn = "FindListOn";
        owner.setFirstName(findListOn);
        given(clinicService.findOwnerByLastName(any()))
                .willReturn(Lists.newArrayList(owner));
        mockMvc.perform(get("/owners")
                        .param("lastName",findListOn))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/1"));
        then(clinicService).should().findOwnerByLastName(anyString());
    }

    @Test
    void processFindFormTest() throws Exception {
        mockMvc.perform(get("/owners")
                        .param("lastName", "Dont find ME!"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/findOwners"));
    }

    @Test
    void initCreationFormTest() throws Exception{
        mockMvc.perform(get("/owners/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("owner"))
                .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void tempTest() {
        assertNotNull(ownerController);
        assertNotNull(clinicService);
    }
}