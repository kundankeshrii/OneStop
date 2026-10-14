
const placementForm = document.getElementById("placementForm");


const generateBtn = document.getElementById("generateBtn");
const loadingSection = document.getElementById("loadingSection");
const resultSection = document.getElementById("resultSection");

const skillsRequiredContainer =
    document.getElementById("skillsRequired");

const hiringProcessContainer =
    document.getElementById("hiringProcess");

const preparationPriorityContainer =
    document.getElementById("preparationPriority");

const previouslyAskedQuestionsContainer =
    document.getElementById("previouslyAskedQuestions");

const preparationRoadmapContainer =
    document.getElementById("preparationRoadmap");

const resultTitle =
    document.getElementById("resultTitle");

const resultSubtitle =
    document.getElementById("resultSubtitle");

const newPlanBtn =
    document.getElementById("newPlanBtn");


// ============================================
// BACKEND URL
// ============================================

const API_URL = "http://localhost:8080/api/placement/generate";


// ============================================
// GENERATE PLACEMENT PLAN
// ============================================

placementForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const targetCompany =
        document.getElementById("targetCompany").value.trim();

    const targetRole =
        document.getElementById("targetRole").value.trim();

    const timeLeftDays =
        Number(document.getElementById("timeLeftDays").value);


    // Basic frontend validation

    if (!targetCompany || !targetRole || !timeLeftDays) {
        alert("Please fill all the required fields.");
        return;
    }



    function validateCollegeExamCourse(
        examType,
        preferredCourse
    ) {

        const exam =
            String(examType)
                .trim()
                .toLowerCase();

        const course =
            String(preferredCourse)
                .trim()
                .toLowerCase();

        if (
            exam === "neet" &&
            (
                course.includes("engineering") ||
                course === "cse" ||
                course === "ece" ||
                course === "eee" ||
                course === "me" ||
                course === "ce"
            )
        ) {

            return {
                valid: false,
                message:
                    "NEET is not applicable for engineering courses. Please select JEE Main for engineering college prediction."
            };
        }

        return {
            valid: true
        };
    }
    // ========================================
    // EXACT PlacementRequest STRUCTURE
    // ========================================

    const requestData = {
        targetCompany: targetCompany,
        targetRole: targetRole,
        timeLeftDays: timeLeftDays
    };


    try {

        // Show loading

        generateBtn.disabled = true;

        loadingSection.classList.remove("hidden");

        resultSection.classList.add("hidden");


        // ====================================
        // SEND REQUEST TO SPRING BOOT
        // ====================================
        const token = localStorage.getItem("onestop_token");

        const response = await fetch(API_URL, {

            method: "POST",

            headers: {
                "Content-Type": "application/json",

                ...(token && {
                    "Authorization": `Bearer ${token}`
                })
            },

            body: JSON.stringify(requestData)

        });


        if (!response.ok) {

            throw new Error(
                `Server returned ${response.status}`
            );

        }


        // ====================================
        // GET PlacementPlanResponse
        // ====================================

        const data = await response.json();


        console.log("Placement Response:");
        console.log(data);


        // ====================================
        // DISPLAY RESULT
        // ====================================

        displayPlacementPlan(data);


    } catch (error) {

        console.error("Error:", error);

        alert(
            "Unable to generate placement plan. " +
            "Please make sure the Spring Boot server is running."
        );

    } finally {

        generateBtn.disabled = false;

        loadingSection.classList.add("hidden");

    }

});


// ============================================
// DISPLAY PLACEMENT PLAN
// ============================================

function displayPlacementPlan(data) {

    /*
       These names are EXACTLY your backend
       PlacementPlanResponse variables.
    */

    renderSkillsRequired(
        data.skillsRequired
    );

    renderHiringProcess(
        data.hiringProcess
    );

    renderPreparationPriority(
        data.preparationPriority
    );

    renderPreviouslyAskedQuestions(
        data.previouslyAskedQuestions
    );

    renderPreparationRoadmap(
        data.preparationRoadmap
    );


    // Result heading

    const company =
        document.getElementById("targetCompany").value;

    const role =
        document.getElementById("targetRole").value;

    const days =
        document.getElementById("timeLeftDays").value;


    resultTitle.textContent =
        `${company} • ${role}`;

    resultSubtitle.textContent =
        `${days}-day placement preparation plan`;


    // Show result

    resultSection.classList.remove("hidden");

    resultSection.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

}


// ============================================
// 01. SKILLS REQUIRED
// ============================================

function renderSkillsRequired(skillsRequired) {

    skillsRequiredContainer.innerHTML = "";

    if (!skillsRequired || skillsRequired.length === 0) {
        return;
    }


    skillsRequired.forEach(skillItem => {

        const skillTag =
            document.createElement("div");

        skillTag.className = "skill-tag";


        /*
           Backend variables:

           priority
           skill
           topics
        */

        skillTag.textContent =
            skillItem.skill;


        skillTag.title =
            skillItem.topics || "";


        skillsRequiredContainer.appendChild(
            skillTag
        );

    });

}


// ============================================
// 02. HIRING PROCESS
// ============================================

function renderHiringProcess(hiringProcess) {

    hiringProcessContainer.innerHTML = "";

    if (!hiringProcess || hiringProcess.length === 0) {
        return;
    }


    hiringProcess.forEach((stage, index) => {

        const hiringItem =
            document.createElement("div");

        hiringItem.className =
            "hiring-item";


        hiringItem.innerHTML = `

            <div class="hiring-dot"></div>

            <div class="hiring-line"></div>

            <div class="hiring-content">

                <span class="hiring-stage">
                    ${escapeHtml(stage.stage)}
                </span>

                <span class="hiring-focus">
                    ${escapeHtml(stage.focus)}
                </span>

            </div>

        `;


        hiringProcessContainer.appendChild(
            hiringItem
        );

    });

}


// ============================================
// 03. PREPARATION PRIORITY
// ============================================

function renderPreparationPriority(preparationPriority) {

    preparationPriorityContainer.innerHTML = "";

    if (
        !preparationPriority ||
        preparationPriority.length === 0
    ) {
        return;
    }


    preparationPriority.forEach(priority => {

        const priorityItem =
            document.createElement("div");

        priorityItem.className =
            "priority-item";


        priorityItem.innerHTML = `

            <span class="priority-rank">
                #${priority.rank}
            </span>

            <span class="priority-area">
                ${escapeHtml(priority.area)}
            </span>

            <span class="priority-days">
                ${priority.recommendedDays} days
            </span>

        `;


        preparationPriorityContainer.appendChild(
            priorityItem
        );

    });

}


// ============================================
// 04. PREVIOUSLY ASKED QUESTIONS
// ============================================

function renderPreviouslyAskedQuestions(
    previouslyAskedQuestions
) {

    previouslyAskedQuestionsContainer.innerHTML = "";

    if (
        !previouslyAskedQuestions ||
        previouslyAskedQuestions.length === 0
    ) {
        return;
    }


    previouslyAskedQuestions.forEach(
        (item, index) => {

            const questionItem =
                document.createElement("div");

            questionItem.className =
                "question-item";


            questionItem.innerHTML = `

                <button
                    class="question-button"
                    type="button"
                >

                    <span class="question-number">
                        ${String(index + 1).padStart(2, "0")}
                    </span>

                    <span class="question-text">
                        ${escapeHtml(item.question)}
                    </span>

                    <span class="question-plus">
                        +
                    </span>

                </button>


                <div class="question-answer">

                    <div class="answer-label">
                        Answer
                    </div>

                    <div class="answer-text">
                        ${escapeHtml(item.answer)}
                    </div>

                </div>

            `;


            const button =
                questionItem.querySelector(
                    ".question-button"
                );


            button.addEventListener(
                "click",
                function () {

                    questionItem.classList.toggle(
                        "open"
                    );

                }
            );


            previouslyAskedQuestionsContainer.appendChild(
                questionItem
            );

        }
    );

}
function toggleQuestion(index) {

    const answer = document.getElementById(`answer-${index}`);
    const toggle = document.getElementById(`toggle-${index}`);

    const isOpen = answer.classList.contains("show");

    // Optional: close all other answers
    document.querySelectorAll(".question-answer")
        .forEach(element => {
            element.classList.remove("show");
        });

    document.querySelectorAll(".question-toggle")
        .forEach(element => {
            element.textContent = "+";
        });

    if (!isOpen) {
        answer.classList.add("show");
        toggle.textContent = "−";
    }
}


// ============================================
// 05. PREPARATION ROADMAP
// ============================================

function renderPreparationRoadmap(
    preparationRoadmap
) {

    preparationRoadmapContainer.innerHTML = "";

    if (
        !preparationRoadmap ||
        preparationRoadmap.length === 0
    ) {
        return;
    }


    preparationRoadmap.forEach(
        roadmapDay => {

            const roadmapItem =
                document.createElement("div");

            roadmapItem.className =
                "roadmap-item";


            /*
               RoadmapDay EXACT backend variables:

               days
               focus
            */

            roadmapItem.innerHTML = `

                <span class="roadmap-days">
                    ${escapeHtml(roadmapDay.days)}
                </span>

                <span class="roadmap-arrow">
                    →
                </span>

                <span class="roadmap-focus">
                    ${escapeHtml(roadmapDay.focus)}
                </span>

            `;


            preparationRoadmapContainer.appendChild(
                roadmapItem
            );

        }
    );

}


// ============================================
// NEW PLAN
// ============================================

newPlanBtn.addEventListener(
    "click",
    function () {

        resultSection.classList.add("hidden");

        placementForm.reset();

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

    }
);


// ============================================
// NEW PREPARATION BUTTON
// ============================================

const newChatBtn =
    document.getElementById("newChatBtn");

if (newChatBtn) {

    newChatBtn.addEventListener(
        "click",
        function () {

            showOneStopHome();

        }
    );

}

// ============================================
// MOBILE SIDEBAR
// ============================================

const mobileMenuBtn =
    document.getElementById("mobileMenuBtn");

const sidebar =
    document.querySelector(".sidebar");


if (mobileMenuBtn) {

    mobileMenuBtn.addEventListener(
        "click",
        function () {

            sidebar.classList.toggle("open");

        }
    );

}


// ============================================
// SAFE HTML
// ============================================

function escapeHtml(value) {

    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");

}


// ============================================================
// FEATURE NAVIGATION
// ============================================================

const onestopHome =
    document.getElementById("onestopHome");

const placementPage =
    document.getElementById("placementPage");

const scholarshipPage =
    document.getElementById("scholarshipPage");

const featureItems =
    document.querySelectorAll(".feature-item");


// ============================================================
// SHOW PLACEMENT PAGE
// ============================================================

function showPlacementPage() {

    // Hide landing page
    onestopHome.classList.add("hidden");

    // Hide other modules
    scholarshipPage.classList.add("hidden");

    const collegePage =
        document.getElementById("collegePredictionPage");

    if (collegePage) {
        collegePage.classList.add("hidden");
    }

    // Show Placement
    placementPage.classList.remove("hidden");


    // Active sidebar item
    featureItems.forEach(item => {
        item.classList.remove("active");
    });

    const placementItem =
        document.querySelector(
            '.feature-item[data-feature="placement"]'
        );

    if (placementItem) {
        placementItem.classList.add("active");
    }


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}


// ============================================================
// SHOW SCHOLARSHIP PAGE
// ============================================================

function showScholarshipPage() {

    // Hide landing page
    onestopHome.classList.add("hidden");

    // Hide other modules
    placementPage.classList.add("hidden");

    const collegePage =
        document.getElementById("collegePredictionPage");

    if (collegePage) {
        collegePage.classList.add("hidden");
    }

    // Show Scholarship
    scholarshipPage.classList.remove("hidden");


    // Active sidebar item
    featureItems.forEach(item => {
        item.classList.remove("active");
    });

    const scholarshipItem =
        document.querySelector(
            '.feature-item[data-feature="scholarship"]'
        );

    if (scholarshipItem) {
        scholarshipItem.classList.add("active");
    }


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}


// ============================================================
// SIDEBAR FEATURE CLICK
// ============================================================

featureItems.forEach(featureItem => {

    featureItem.addEventListener(
        "click",
        function () {

            const feature =
                this.dataset.feature;


            // -----------------------------
            // PLACEMENT
            // -----------------------------

            if (feature === "placement") {

                showPlacementPage();

            }


            // -----------------------------
            // SCHOLARSHIP
            // -----------------------------

            else if (feature === "scholarship") {

                showScholarshipPage();

            }


            // -----------------------------
            // COLLEGE PREDICTION
            // -----------------------------

            else if (feature === "college") {

                showCollegePredictionPage();

            }
            else if (feature === "history") {

                // ==================================================
                // HISTORY REQUIRES LOGIN
                // ==================================================

                const token =
                    localStorage.getItem("onestop_token");

                // Guest user → open login modal
                if (!token) {

                    if (typeof openAuthModal === "function") {

                        openAuthModal("login");

                    } else {

                        alert(
                            "Please sign in to view your history."
                        );
                    }

                    return;
                }


                // ==================================================
                // LOGGED-IN USER → SHOW HISTORY
                // ==================================================

                // Hide landing page
                onestopHome.classList.add("hidden");

                // Hide all existing modules
                placementPage.classList.add("hidden");
                scholarshipPage.classList.add("hidden");
                collegePredictionPage.classList.add("hidden");

                // Show History
                historyPage.classList.remove("hidden");

                // Load database history
                loadHistory();

                // Active sidebar item
                featureItems.forEach(item => {
                    item.classList.remove("active");
                });

                this.classList.add("active");

                // Scroll to top
                window.scrollTo({
                    top: 0,
                    behavior: "smooth"
                });

            }
        }
    );

}
);




// ============================================
// SCHOLARSHIP FINDER
// ============================================

const scholarshipForm =
    document.getElementById("scholarshipForm");

const scholarshipFindBtn =
    document.getElementById("scholarshipFindBtn");

const scholarshipLoadingSection =
    document.getElementById("scholarshipLoadingSection");

const scholarshipResults =
    document.getElementById("scholarshipResults");

const scholarshipTableBody =
    document.getElementById("scholarshipTableBody");

const scholarshipErrorSection =
    document.getElementById("scholarshipErrorSection");

const scholarshipErrorMessage =
    document.getElementById("scholarshipErrorMessage");

const scholarshipEmptySection =
    document.getElementById("scholarshipEmptySection");

const scholarshipFormError =
    document.getElementById("scholarshipFormError");

const SCHOLARSHIP_API_URL =
    "http://localhost:8080/api/scholarships/find";


// ============================================================
// SCHOLARSHIP ACADEMIC STAGE
// ============================================================

const educationStage =
    document.getElementById("educationStage");

const currentYear =
    document.getElementById("currentYear");

const lastQualifyingExam =
    document.getElementById("lastQualifyingExam");


if (
    educationStage &&
    currentYear &&
    lastQualifyingExam
) {

    educationStage.addEventListener(
        "change",
        function () {

            const stage = this.value;

            currentYear.innerHTML =
                '<option value="">Select year / semester</option>';

            lastQualifyingExam.value = "";

            currentYear.disabled = false;


            // =========================
            // CLASS 11
            // =========================

            if (stage === "11th") {

                currentYear.innerHTML =
                    '<option value="11th">Class 11</option>';

                currentYear.value = "11th";

                lastQualifyingExam.value =
                    "Class 10";
            }


            // =========================
            // CLASS 12
            // =========================

            else if (stage === "12th") {

                currentYear.innerHTML =
                    '<option value="12th">Class 12</option>';

                currentYear.value = "12th";

                lastQualifyingExam.value =
                    "Class 11";
            }


            // =========================
            // DIPLOMA
            // =========================

            else if (stage === "Diploma") {

                currentYear.innerHTML +=
                    '<option value="1st Year">1st Year</option>';

                currentYear.innerHTML +=
                    '<option value="2nd Year">2nd Year</option>';

                currentYear.innerHTML +=
                    '<option value="3rd Year">3rd Year</option>';
            }


            // =========================
            // UNDERGRADUATE
            // =========================

            else if (stage === "Undergraduate") {

                currentYear.innerHTML +=
                    '<option value="1st Year">1st Year</option>';

                currentYear.innerHTML +=
                    '<option value="2nd Year">2nd Year</option>';

                currentYear.innerHTML +=
                    '<option value="3rd Year">3rd Year</option>';

                currentYear.innerHTML +=
                    '<option value="4th Year">4th Year</option>';
            }


            // =========================
            // POSTGRADUATE
            // =========================

            else if (stage === "Postgraduate") {

                currentYear.innerHTML +=
                    '<option value="1st Year">1st Year</option>';

                currentYear.innerHTML +=
                    '<option value="2nd Year">2nd Year</option>';
            }

        }
    );


    // ========================================================
    // CURRENT YEAR CHANGE
    // ========================================================

    currentYear.addEventListener(
        "change",
        function () {

            const stage =
                educationStage.value;

            const year =
                this.value;


            // =========================
            // DIPLOMA
            // =========================

            if (stage === "Diploma") {

                if (year === "1st Year") {

                    lastQualifyingExam.value =
                        "Class 10 / Class 12";

                }

                else if (year === "2nd Year") {

                    lastQualifyingExam.value =
                        "Diploma 1st Year";

                }

                else if (year === "3rd Year") {

                    lastQualifyingExam.value =
                        "Diploma 2nd Year";

                }

            }


            // =========================
            // UNDERGRADUATE
            // =========================

            else if (stage === "Undergraduate") {

                if (year === "1st Year") {

                    lastQualifyingExam.value =
                        "Class 12 / Diploma";

                }

                else if (year === "2nd Year") {

                    lastQualifyingExam.value =
                        "Undergraduate 1st Year";

                }

                else if (year === "3rd Year") {

                    lastQualifyingExam.value =
                        "Undergraduate 2nd Year";

                }

                else if (year === "4th Year") {

                    lastQualifyingExam.value =
                        "Undergraduate 3rd Year";

                }

            }


            // =========================
            // POSTGRADUATE
            // =========================

            else if (stage === "Postgraduate") {

                if (year === "1st Year") {

                    lastQualifyingExam.value =
                        "Undergraduate Final Year";

                }

                else if (year === "2nd Year") {

                    lastQualifyingExam.value =
                        "Postgraduate 1st Year";

                }

            }

        }
    );

}


// ============================================================
// SCHOLARSHIP FORM SUBMIT
// ============================================================

if (scholarshipForm) {

    scholarshipForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            scholarshipResults.classList.add("hidden");

            scholarshipErrorSection.classList.add("hidden");

            scholarshipEmptySection.classList.add("hidden");

            scholarshipFormError.classList.add("hidden");


            // ==================================================
            // REQUEST DATA
            // ==================================================

            const requestData = {

                state:
                    document
                        .getElementById("scholarshipState")
                        .value
                        .trim(),

                category:
                    document
                        .getElementById("scholarshipCategory")
                        .value,

                annualFamilyIncome:
                    Number(
                        document
                            .getElementById("annualFamilyIncome")
                            .value
                    ),

                course:
                    document
                        .getElementById("scholarshipCourse")
                        .value
                        .trim(),


                // ==============================================
                // NEW ACADEMIC FIELDS
                // ==============================================

                educationStage:
                    document
                        .getElementById("educationStage")
                        .value,

                currentYear:
                    document
                        .getElementById("currentYear")
                        .value
                        .trim(),

                lastQualifyingExam:
                    document
                        .getElementById("lastQualifyingExam")
                        .value
                        .trim(),

                lastQualifyingExamPercentage:
                    Number(
                        document
                            .getElementById(
                                "lastQualifyingExamPercentage"
                            )
                            .value
                    ),


                // ==============================================
                // OTHER STUDENT INFORMATION
                // ==============================================

                gender:
                    document
                        .getElementById("scholarshipGender")
                        .value,

                pwd:
                    document
                        .getElementById("scholarshipPwd")
                        .value === "true",

                parentOccupation:
                    document
                        .getElementById("parentOccupation")
                        .value
                        .trim()
            };


            console.log(
                "Scholarship Request:"
            );

            console.log(requestData);


            // ==================================================
            // API REQUEST
            // ==================================================

            try {

                scholarshipFindBtn.disabled = true;

                scholarshipLoadingSection.classList.remove(
                    "hidden"
                );

                const token = localStorage.getItem("onestop_token");

                const response = await fetch(SCHOLARSHIP_API_URL, {

                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",

                        ...(token && {
                            "Authorization": `Bearer ${token}`
                        })
                    },

                    body: JSON.stringify(requestData)

                });


                if (!response.ok) {

                    throw new Error(
                        `Server returned ${response.status}`
                    );

                }


                const data =
                    await response.json();


                console.log(
                    "Scholarship Response:"
                );

                console.log(data);


                displayScholarships(data);


            } catch (error) {

                console.error(
                    "Scholarship Error:",
                    error
                );


                scholarshipErrorMessage.textContent =
                    "Unable to find scholarships right now. Please try again.";


                scholarshipErrorSection.classList.remove(
                    "hidden"
                );


            } finally {

                scholarshipFindBtn.disabled = false;

                scholarshipLoadingSection.classList.add(
                    "hidden"
                );

            }

        }
    );

}


// ============================================
// DISPLAY SCHOLARSHIPS
// ============================================

function displayScholarships(data) {

    scholarshipTableBody.innerHTML = "";


    const scholarships =
        data.scholarshipResultList || [];


    // ========================================================
    // NO MATCHING SCHOLARSHIPS
    // ========================================================

    if (scholarships.length === 0) {

        scholarshipEmptySection.classList.remove(
            "hidden"
        );

        return;
    }


    // ========================================================
    // MAXIMUM 5 SCHOLARSHIPS
    // ========================================================

    scholarships
        .slice(0, 5)
        .forEach(
            scholarship => {

                const row =
                    document.createElement("tr");


                row.innerHTML = `

                    <td>
                        ${escapeHtml(
                    scholarship.name
                )}
                    </td>

                    <td>
                        ${escapeHtml(
                    scholarship.provider
                )}
                    </td>

                    <td>
                        ${escapeHtml(
                    scholarship.eligibility
                )}
                    </td>

                    <td>
                        ${escapeHtml(
                    scholarship.amount
                )}
                    </td>

                    <td>
                        ${escapeHtml(
                    scholarship.deadline
                )}
                    </td>

                    <td>
                        <span class="scholarship-match-badge">
                            ${scholarship.matchScore}%
                        </span>
                    </td>

                    <td>
                        <span class="scholarship-status-badge">
                            ${escapeHtml(
                    scholarship.status
                )}
                        </span>
                    </td>

                    <td>
                        ${scholarship.applicationLink
                        ? `
                                    <a
                                        href="${escapeHtml(
                            scholarship.applicationLink
                        )}"
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        class="scholarship-apply-btn"
                                    >
                                        Apply
                                    </a>
                                  `
                        : `
                                    <span>
                                        View Details
                                    </span>
                                  `
                    }
                    </td>

                `;


                scholarshipTableBody.appendChild(
                    row
                );

            }
        );


    scholarshipResults.classList.remove(
        "hidden"
    );


    scholarshipResults.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

}






// ============================================================
// SHOW ONESTOP LANDING PAGE
// ============================================================

function showOneStopHome() {

    // Show landing page
    onestopHome.classList.remove("hidden");


    // Hide Placement
    placementPage.classList.add("hidden");


    // Hide Scholarship
    scholarshipPage.classList.add("hidden");


    // Hide College Prediction
    // We use getElementById because the College
    // variable is declared later in this file.
    const collegePage =
        document.getElementById("collegePredictionPage");

    if (collegePage) {

        collegePage.classList.add("hidden");

    }


    // Remove active sidebar item
    document
        .querySelectorAll(".feature-item")
        .forEach(item => {

            item.classList.remove("active");

        });


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });

}


// ============================================================
// OPEN LANDING PAGE WHEN APPLICATION LOADS
// ============================================================

showOneStopHome();

// ============================================================
// LANDING SERVICE NAVIGATION
// ============================================================

const onestopServiceCards =
    document.querySelectorAll(".onestop-service-card");


onestopServiceCards.forEach(card => {

    card.addEventListener("click", function () {

        const service =
            this.dataset.service;


        // -----------------------------
        // PLACEMENT
        // -----------------------------

        if (service === "placement") {

            onestopHome.classList.add("hidden");

            placementPage.classList.remove("hidden");

            scholarshipPage.classList.add("hidden");

            document
                .querySelectorAll(".feature-item")
                .forEach(item => {
                    item.classList.remove("active");
                });

            const placementItem =
                document.querySelector(
                    '.feature-item[data-feature="placement"]'
                );

            if (placementItem) {
                placementItem.classList.add("active");
            }

            window.scrollTo({
                top: 0,
                behavior: "smooth"
            });

        }


        // -----------------------------
        // SCHOLARSHIP
        // -----------------------------

        if (service === "scholarship") {

            onestopHome.classList.add("hidden");

            placementPage.classList.add("hidden");

            scholarshipPage.classList.remove("hidden");

            document
                .querySelectorAll(".feature-item")
                .forEach(item => {
                    item.classList.remove("active");
                });

            const scholarshipItem =
                document.querySelector(
                    '.feature-item[data-feature="scholarship"]'
                );

            if (scholarshipItem) {
                scholarshipItem.classList.add("active");
            }

            window.scrollTo({
                top: 0,
                behavior: "smooth"
            });

        }


        // -----------------------------
        // CAREER GUIDANCE
        // -----------------------------

        if (service === "career") {

            alert(
                "Career Guidance is coming soon."
            );

        }


        // -----------------------------
        // COLLEGE PREDICTOR
        // -----------------------------

        if (service === "college") {

            showCollegePredictionPage();

        }

    });

});


const oneStopLogo =
    document.querySelector(".logo");


if (oneStopLogo) {

    oneStopLogo.style.cursor = "pointer";

    oneStopLogo.addEventListener(
        "click",
        showOneStopHome
    );

}

// ============================================================
// MODULE 3 — COLLEGE PREDICTION
// New code only — existing modules remain untouched
// ============================================================

const collegePredictionPage =
    document.getElementById("collegePredictionPage");

const collegePredictionForm =
    document.getElementById("collegePredictionForm");

const collegePredictionBtn =
    document.getElementById("collegePredictionBtn");

const collegePredictionLoading =
    document.getElementById("collegePredictionLoading");

const collegePredictionResults =
    document.getElementById("collegePredictionResults");

const collegePredictionTableBody =
    document.getElementById("collegePredictionTableBody");

const collegePredictionEmpty =
    document.getElementById("collegePredictionEmpty");

const collegePredictionError =
    document.getElementById("collegePredictionError");

const collegePredictionErrorMessage =
    document.getElementById("collegePredictionErrorMessage");

const collegePredictionFormError =
    document.getElementById("collegePredictionFormError");

const collegePredictionNewBtn =
    document.getElementById("collegePredictionNewBtn");


// ============================================================
// BACKEND URL
// ============================================================

const COLLEGE_PREDICTION_API_URL =
    "http://localhost:8080/api/college-prediction/predict";


// ============================================================
// SHOW COLLEGE PREDICTION PAGE
// ============================================================

function showCollegePredictionPage() {

    onestopHome.classList.add("hidden");

    placementPage.classList.add("hidden");

    scholarshipPage.classList.add("hidden");

    collegePredictionPage.classList.remove("hidden");


    document
        .querySelectorAll(".feature-item")
        .forEach(item => {
            item.classList.remove("active");
        });


    const collegeItem =
        document.querySelector(
            '.feature-item[data-feature="college"]'
        );


    if (collegeItem) {
        collegeItem.classList.add("active");
    }


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });

}




// ============================================================
// COLLEGE PREDICTION FORM
// ============================================================

collegePredictionForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        collegePredictionFormError.classList.add("hidden");
        collegePredictionResults.classList.add("hidden");
        collegePredictionEmpty.classList.add("hidden");
        collegePredictionError.classList.add("hidden");


        // ====================================================
        // GET VALUES
        // ====================================================

        const tenthValue =
            document
                .getElementById("collegeTenthPercentage")
                .value
                .trim();

        const twelfthValue =
            document
                .getElementById("collegeTwelfthPercentage")
                .value
                .trim();

        const diplomaValue =
            document
                .getElementById("collegeDiplomaPercentage")
                .value
                .trim();

        const examType =
            document
                .getElementById("collegeExamType")
                .value;

        const examRankPercentile =
            document
                .getElementById("collegeExamRankPercentile")
                .value
                .trim();

        const gender =
            document
                .getElementById("collegeGender")
                .value;

        const category =
            document
                .getElementById("collegeCategory")
                .value;

        const pwd =
            document
                .getElementById("collegePwd")
                .value;

        const preferredCourse =
            document
                .getElementById("collegePreferredCourse")
                .value;

        const examCourseValidation =
            validateCollegeExamCourse(
                examType,
                preferredCourse
            );

        if (!examCourseValidation.valid) {

            showCollegePredictionFormError(
                examCourseValidation.message
            );

            return;
        }

        const quota =
            document
                .getElementById("collegeQuota")
                .value;

        const rankType =
            document
                .getElementById("collegeRankType")
                .value;

        const collegeType =
            document
                .getElementById("collegeType")
                .value;

        const targetCollege =
            document
                .getElementById("collegeTargetCollege")
                .value
                .trim();



        // ============================================================
        // VALIDATE EXAM + COURSE
        // ============================================================

        function validateCollegeExamCourse(
            examType,
            preferredCourse
        ) {

            const exam =
                String(examType || "")
                    .trim()
                    .toUpperCase();

            const course =
                String(preferredCourse || "")
                    .trim()
                    .toUpperCase();

            if (!exam || !course) {
                return {
                    valid: true,
                    message: ""
                };
            }

            /*
             * JEE Main / JEE Advanced
             * Engineering courses
             */
            if (
                (exam === "JEE MAIN" ||
                    exam === "JEE ADVANCED") &&
                (
                    course.includes("B.TECH") ||
                    course.includes("B.E") ||
                    course.includes("ENGINEERING") ||
                    course === "CSE" ||
                    course === "ECE" ||
                    course === "EEE" ||
                    course === "ME" ||
                    course === "CE"
                )
            ) {
                return {
                    valid: true,
                    message: ""
                };
            }

            /*
             * NEET
             * Medical courses only
             */
            if (
                exam === "NEET" &&
                (
                    course.includes("MBBS") ||
                    course.includes("BDS") ||
                    course.includes("BHMS") ||
                    course.includes("BAMS") ||
                    course.includes("BUMS") ||
                    course.includes("MEDICAL")
                )
            ) {
                return {
                    valid: true,
                    message: ""
                };
            }

            /*
             * CUET
             */
            if (exam === "CUET") {
                return {
                    valid: true,
                    message: ""
                };
            }

            return {
                valid: false,
                message:
                    `${examType} is not compatible with ${preferredCourse}.`
            };
        }
        // ====================================================
        // FRONTEND VALIDATION
        // ====================================================

        if (!tenthValue) {

            showCollegePredictionFormError(
                "Please enter your 10th percentage."
            );

            return;
        }

        if (!examType) {

            showCollegePredictionFormError(
                "Please select your exam type."
            );

            return;
        }

        if (!examRankPercentile) {

            showCollegePredictionFormError(
                "Please enter your exam rank/percentile."
            );

            return;
        }

        if (!gender) {

            showCollegePredictionFormError(
                "Please select your gender."
            );

            return;
        }

        if (!category) {

            showCollegePredictionFormError(
                "Please select your category."
            );

            return;
        }

        if (!pwd) {

            showCollegePredictionFormError(
                "Please select your PwD status."
            );

            return;
        }

        if (!preferredCourse) {

            showCollegePredictionFormError(
                "Please select your preferred course."
            );

            return;
        }


        // ====================================================
        // PERCENTAGE VALIDATION
        // ====================================================

        if (!isValidCollegePercentage(tenthValue)) {

            showCollegePredictionFormError(
                "10th percentage must be between 0 and 100."
            );

            return;
        }

        if (
            twelfthValue &&
            !isValidCollegePercentage(twelfthValue)
        ) {

            showCollegePredictionFormError(
                "12th percentage must be between 0 and 100."
            );

            return;
        }

        if (
            diplomaValue &&
            !isValidCollegePercentage(diplomaValue)
        ) {

            showCollegePredictionFormError(
                "Diploma percentage must be between 0 and 100."
            );

            return;
        }


        // ====================================================
        // REQUEST DATA
        // ====================================================

        const requestData = {

            tenthPercentage:
                Number(tenthValue),

            twelfthPercentage:
                twelfthValue
                    ? Number(twelfthValue)
                    : null,

            diplomaPercentage:
                diplomaValue
                    ? Number(diplomaValue)
                    : null,

            examType:
                examType,

            examRankPercentile:
                examRankPercentile,

            rankType:
                rankType,

            gender:
                gender,

            category:
                category,

            pwd:
                pwd,

            preferredCourse:
                preferredCourse,

            quota:
                quota || null,

            collegeType:
                collegeType || null,

            targetCollege:
                targetCollege || null
        };


        console.log(
            "College Prediction Request:"
        );

        console.log(requestData);


        // ====================================================
        // SEND TO SPRING BOOT
        // ====================================================

        try {

            collegePredictionBtn.disabled = true;

            collegePredictionLoading.classList.remove(
                "hidden"
            );


            const token =
                localStorage.getItem("onestop_token");

            const response = await fetch(
                COLLEGE_PREDICTION_API_URL,
                {

                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",

                        ...(token && {
                            "Authorization": `Bearer ${token}`
                        })
                    },

                    body: JSON.stringify(requestData)

                }
            );


            const data =
                await response.json();


            console.log(
                "College Prediction Response:"
            );

            console.log(data);


            // =================================================
            // SERVER ERROR
            // =================================================

            if (!response.ok) {

                throw new Error(
                    data.message ||
                    data.error ||
                    `Server returned ${response.status}`
                );
            }


            // =================================================
            // EMPTY RESULT
            // =================================================

            if (
                !data.collegeResultList ||
                data.collegeResultList.length === 0
            ) {

                showNoResultsMessage();

                return;
            }


            // =================================================
            // DISPLAY RESULTS
            // =================================================

            displayCollegePredictions(data);


            // =================================================
            // DISPLAY AI RECOMMENDATIONS
            // =================================================




        } catch (error) {

            console.error(
                "College Prediction Error:",
                error
            );


            collegePredictionErrorMessage.textContent =
                "Unable to predict colleges right now. Please make sure the Spring Boot server is running and try again.";


            collegePredictionError.classList.remove(
                "hidden"
            );


        } finally {

            collegePredictionBtn.disabled = false;

            collegePredictionLoading.classList.add(
                "hidden"
            );

        }

    }
);


// ============================================================
// SHOW NO RESULTS
// ============================================================

function showNoResultsMessage() {

    collegePredictionEmpty.classList.remove(
        "hidden"
    );

}


// ============================================================
// DISPLAY COLLEGE PREDICTIONS
// ============================================================

function displayCollegePredictions(data) {

    collegePredictionTableBody.innerHTML = "";


    const colleges =
        data.collegeResultList || [];


    if (colleges.length === 0) {

        collegePredictionEmpty.classList.remove(
            "hidden"
        );

        return;
    }


    colleges.forEach(
        college => {

            const row =
                document.createElement("tr");


            row.innerHTML = `

                <td>
                    ${escapeHtml(
                college.college || ""
            )}
                </td>

                <td>
                    ${escapeHtml(
                college.openingClosingRank || ""
            )}
                </td>

                <td>
                    ${escapeHtml(
                college.branch || ""
            )}
                </td>

                <td>
                    ${escapeHtml(
                college.fees || ""
            )}
                </td>

                <td>
                    ${escapeHtml(
                college.avgPackage || ""
            )}
                </td>

                <td>
                    ${escapeHtml(
                college.counselling || ""
            )}
                </td>

            `;


            collegePredictionTableBody.appendChild(
                row
            );

        }
    );


    collegePredictionResults.classList.remove(
        "hidden"
    );


    collegePredictionResults.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });

}




// ============================================================
// VALIDATE PERCENTAGE
// ============================================================

function isValidCollegePercentage(
    value
) {

    const percentage =
        Number(value);


    return (
        !Number.isNaN(percentage) &&
        percentage >= 0 &&
        percentage <= 100
    );

}


// ============================================================
// FORM ERROR
// ============================================================

function showCollegePredictionFormError(
    message
) {

    collegePredictionFormError.textContent =
        message;


    collegePredictionFormError.classList.remove(
        "hidden"
    );


    collegePredictionFormError.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });

}


// ============================================================
// NEW PREDICTION
// ============================================================

collegePredictionNewBtn.addEventListener(
    "click",
    function () {

        collegePredictionResults.classList.add(
            "hidden"
        );

        collegePredictionEmpty.classList.add(
            "hidden"
        );

        collegePredictionError.classList.add(
            "hidden"
        );

        collegePredictionFormError.classList.add(
            "hidden"
        );


        const recommendations =
            document.getElementById(
                "collegeRecommendations"
            );


        if (recommendations) {

            recommendations.innerHTML = "";

            recommendations.classList.add(
                "hidden"
            );
        }


        collegePredictionForm.reset();


        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

    }
);



// ============================================================
// HISTORY
// ============================================================

const HISTORY_API_URL =
    "http://localhost:8080/api/history";


const historyPage =
    document.getElementById("historyPage");

const historyList =
    document.getElementById("historyList");

const historyLoading =
    document.getElementById("historyLoading");

const historyError =
    document.getElementById("historyError");

const historyErrorMessage =
    document.getElementById("historyErrorMessage");

const historyEmpty =
    document.getElementById("historyEmpty");

const historyRetryBtn =
    document.getElementById("historyRetryBtn");

const historyModalOverlay =
    document.getElementById("historyModalOverlay");

const historyModalClose =
    document.getElementById("historyModalClose");

const historyModalModule =
    document.getElementById("historyModalModule");

const historyModalTitle =
    document.getElementById("historyModalTitle");

const historyModalDate =
    document.getElementById("historyModalDate");

const historyRequestData =
    document.getElementById("historyRequestData");

const historyResponseData =
    document.getElementById("historyResponseData");


// ============================================================
// LOAD HISTORY
// ============================================================

async function loadHistory() {

    if (!historyList) {
        return;
    }


    historyLoading.classList.remove("hidden");

    historyError.classList.add("hidden");

    historyEmpty.classList.add("hidden");

    historyList.classList.add("hidden");


    const token =
        localStorage.getItem("onestop_token");


    // --------------------------------------------------------
    // Guest user
    // --------------------------------------------------------

    if (!token) {

        historyLoading.classList.add("hidden");

        historyEmpty.classList.remove("hidden");

        return;
    }


    try {

        const response =
            await fetch(
                HISTORY_API_URL,
                {
                    method: "GET",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        // ----------------------------------------------------
        // Authentication failure
        // ----------------------------------------------------

        if (response.status === 401) {

            localStorage.removeItem(
                "onestop_token"
            );

            historyLoading.classList.add("hidden");

            historyError.classList.remove("hidden");

            historyErrorMessage.textContent =
                "Your session has expired. Please sign in again.";

            return;
        }


        if (!response.ok) {

            throw new Error(
                `Server returned ${response.status}`
            );
        }


        const history =
            await response.json();


        historyLoading.classList.add("hidden");


        // ----------------------------------------------------
        // Empty history
        // ----------------------------------------------------

        if (
            !Array.isArray(history) ||
            history.length === 0
        ) {

            historyEmpty.classList.remove(
                "hidden"
            );

            return;
        }


        // ----------------------------------------------------
        // Render
        // ----------------------------------------------------

        renderHistory(history);

        historyList.classList.remove(
            "hidden"
        );


    } catch (error) {

        console.error(
            "History loading failed:",
            error
        );


        historyLoading.classList.add(
            "hidden"
        );

        historyError.classList.remove(
            "hidden"
        );

        historyErrorMessage.textContent =
            "Unable to load your history. Please try again.";
    }
}

// ============================================================
// RENDER HISTORY
// ============================================================

function renderHistory(history) {

    historyList.innerHTML = "";


    history.forEach(item => {

        const card =
            document.createElement("div");

        card.className =
            "history-card";


        const left =
            document.createElement("div");

        left.className =
            "history-card-left";


        const module =
            document.createElement("span");

        module.className =
            "history-module";

        module.textContent =
            item.module || "MODULE";


        const title =
            document.createElement("h3");

        title.className =
            "history-title";

        title.textContent =
            item.title || "Untitled";


        const date =
            document.createElement("div");

        date.className =
            "history-date";

        date.textContent =
            `Created: ${formatHistoryDate(
                item.createdAt
            )}`;


        left.appendChild(module);

        left.appendChild(title);

        left.appendChild(date);


        // ----------------------------------------------------
        // Actions
        // ----------------------------------------------------

        const actions =
            document.createElement("div");

        actions.className =
            "history-card-actions";


        const viewButton =
            document.createElement("button");

        viewButton.type =
            "button";

        viewButton.className =
            "history-view-btn";

        viewButton.textContent =
            "View";


        viewButton.addEventListener(
            "click",
            () => {

                openHistoryModal(item);

            }
        );


        const deleteButton =
            document.createElement("button");

        deleteButton.type =
            "button";

        deleteButton.className =
            "history-delete-btn";

        deleteButton.textContent =
            "Delete";


        deleteButton.addEventListener(
            "click",
            () => {

                deleteHistoryItem(
                    item.id
                );

            }
        );


        actions.appendChild(
            viewButton
        );

        actions.appendChild(
            deleteButton
        );


        card.appendChild(left);

        card.appendChild(actions);


        historyList.appendChild(card);

    });
}

// ============================================================
// FORMAT DATE
// ============================================================

function formatHistoryDate(dateValue) {

    if (!dateValue) {
        return "Unknown date";
    }


    const date =
        new Date(dateValue);


    if (Number.isNaN(
        date.getTime()
    )) {

        return dateValue;
    }


    return date.toLocaleString(
        "en-IN",
        {
            dateStyle: "medium",
            timeStyle: "short"
        }
    );
}


// ============================================================
// OPEN HISTORY MODAL
// ============================================================

function openHistoryModal(item) {

    historyModalModule.textContent =
        item.module || "HISTORY";


    historyModalTitle.textContent =
        item.title || "History Result";


    historyModalDate.textContent =
        `Created: ${formatHistoryDate(
            item.createdAt
        )}`;


    historyRequestData.textContent =
        formatHistoryJSON(
            item.requestData
        );


    historyResponseData.textContent =
        formatHistoryJSON(
            item.responseData
        );


    historyModalOverlay.classList.remove(
        "hidden"
    );

    document.body.classList.add(
        "auth-open"
    );
}

function formatHistoryJSON(value) {

    if (!value) {
        return "No data available";
    }


    try {

        const parsed =
            typeof value === "string"
                ? JSON.parse(value)
                : value;


        return JSON.stringify(
            parsed,
            null,
            2
        );

    } catch (error) {

        return String(value);
    }
}


// ============================================================
// CLOSE HISTORY MODAL
// ============================================================

function closeHistoryModal() {

    historyModalOverlay.classList.add(
        "hidden"
    );

    document.body.classList.remove(
        "auth-open"
    );
}


if (historyModalClose) {

    historyModalClose.addEventListener(
        "click",
        closeHistoryModal
    );
}


if (historyModalOverlay) {

    historyModalOverlay.addEventListener(
        "click",
        event => {

            if (
                event.target ===
                historyModalOverlay
            ) {

                closeHistoryModal();

            }

        }
    );
}

// ============================================================
// DELETE HISTORY
// ============================================================

async function deleteHistoryItem(id) {

    const confirmed =
        window.confirm(
            "Delete this history item?"
        );


    if (!confirmed) {
        return;
    }


    const token =
        localStorage.getItem(
            "onestop_token"
        );


    if (!token) {

        alert(
            "Please sign in to manage history."
        );

        return;
    }


    try {

        const response =
            await fetch(
                `${HISTORY_API_URL}/${id}`,
                {
                    method: "DELETE",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        if (response.status === 401) {

            localStorage.removeItem(
                "onestop_token"
            );

            alert(
                "Your session has expired. Please sign in again."
            );

            return;
        }


        if (!response.ok) {

            throw new Error(
                `Server returned ${response.status}`
            );
        }


        // Reload history after deletion

        await loadHistory();


    } catch (error) {

        console.error(
            "Delete history failed:",
            error
        );

        alert(
            "Unable to delete history. Please try again."
        );
    }
}


if (historyRetryBtn) {

    historyRetryBtn.addEventListener(
        "click",
        loadHistory
    );
}


