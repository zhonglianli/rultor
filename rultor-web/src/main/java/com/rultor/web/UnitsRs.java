/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.web;

import com.jcabi.aspects.Loggable;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Unit;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * List of units.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Path("/units")
@Loggable(Loggable.DEBUG)
public final class UnitsRs extends BaseRs {

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/index.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(this.mine())
            .link(new Link("create", "./create"))
            .render()
            .build();
    }

    /**
     * Create new empty unit.
     * @param name Name of the unit to create
     * @return The JAX-RS response
     */
    @POST
    @Path("/create")
    public Response create(@NotNull(message = "unit name is mandatory")
        @FormParam("name") final String name) {
        if (this.user().units().contains(name)) {
            throw this.flash().redirect(
                this.uriInfo().getRequestUri(),
                String.format("Unit `%s` already exists", name),
                Level.WARNING
            );
        }
        this.user().units().create(name);
        throw this.flash().redirect(
            this.uriInfo().getBaseUriBuilder()
                .clone()
                .path(UnitRs.class)
                .build(name),
            String.format("Unit `%s` successfully created", name),
            Level.INFO
        );
    }

    /**
     * All my units.
     * @return Collection of JAXB units
     */
    private JaxbBundle mine() {
        return new JaxbBundle("units").add(
            new JaxbBundle.Group<Unit>(this.user().units()) {
                @Override
                public JaxbBundle bundle(final Unit unit) {
                    return UnitsRs.this.unit(unit);
                }
            }
        );
    }

    /**
     * Convert unit to JaxbBundle.
     * @param unit Name of unit
     * @return Bundle
     */
    private JaxbBundle unit(final Unit unit) {
        return new JaxbBundle("unit")
            .add("name", unit.name())
            .up()
            .add(
                new JaxbFace(this.repo(), this.users())
                    .bundle(this.user().urn(), unit.name())
            )
            .link(
                new Link(
                    // @checkstyle MultipleStringLiterals (1 line)
                    "remove",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(UnitRs.class)
                        .path(UnitRs.class, "remove")
                        .build(unit.name())
                )
            )
            .link(
                new Link(
                    "edit",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(UnitRs.class)
                        .build(unit.name())
                )
            )
            .link(
                new Link(
                    "drain",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(DrainRs.class)
                        .build(unit.name())
                )
            );
    }

}
