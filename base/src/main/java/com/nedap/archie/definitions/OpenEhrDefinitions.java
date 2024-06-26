package com.nedap.archie.definitions;

/*
 * #%L
 * OpenEHR - Java Model Stack
 * %%
 * Copyright (C) 2016 - 2017 Cognitive Medical Systems
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 * Author: Claude Nanjo
 */

/**
 * Inheritance class to provide access to constants defined in other packages.
 *
 * Created by cnanjo on 6/2/16.
 */
public class OpenEhrDefinitions  extends BasicDefinitions {
    public static final String LOCAL_TERMINOLOGY_ID = "local";
    public static final String EHR_SCHEME = "ehr";

    // Metadata
    public static final String ADL_VERSION = "adl_version";
    public static final String BUILD_UID = "build_uid";
    public static final String CONTROLLED = "controlled";
    public static final String GENERATED = "generated";
    public static final String RM_RELEASE = "rm_release";
    public static final String UID = "uid";
}