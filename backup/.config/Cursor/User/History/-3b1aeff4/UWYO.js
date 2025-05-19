let currentLanguage = 'both'; // Can be 'en', 'vi', or 'both'

// Start UI-related functions
function switchLanguage(lang) {
    currentLanguage = lang;
    updateLanguageDisplay();

    // Update the button text
    const langButton = document.getElementById('languageSwitch');
    switch (lang) {
        case 'en':
            langButton.innerHTML = '<i class="bi bi-translate text-decoration-none"></i> EN';
            break;
        case 'vi':
            langButton.innerHTML = '<i class="bi bi-translate text-decoration-none"></i> VI';
            break;
        case 'both':
            langButton.innerHTML = '<i class="bi bi-translate text-decoration-none"></i> VI/EN';
            break;
    }
}
function updateLanguageDisplay() {
    const elements = document.querySelectorAll('[data-lang]');
    elements.forEach(element => {
        const langType = element.getAttribute('data-lang');
        if (currentLanguage === 'both' || currentLanguage === langType) {
            element.style.display = '';
        } else {
            element.style.display = 'none';
        }
    });
}
function darkMode() {
    const body = document.body;
    const icon = document.getElementById('darkModeIcon');

    body.classList.toggle('dark-mode');
    icon.classList.add('animated');

    // Update icon
    if (body.classList.contains('dark-mode')) {
        icon.classList.remove('bi-sun-fill');
        icon.classList.add('bi-moon-stars-fill');
    } else {
        icon.classList.remove('bi-moon-stars-fill');
        icon.classList.add('bi-sun-fill');
    }

    // Remove animation class after it completes
    setTimeout(() => {
        icon.classList.remove('animated');
    }, 500);
}
// End UI-related functions




// Validation-related functions
function validateInputs() {

    // Còn thiếu trường hợp nếu người dùng chọn mà không nhập, nhưng vì có thể làm vậy với chủ đích nên không làm
    const setPrice = document.getElementById('set-price').value;
    if (setPrice != 1) {
        const setCount = parseInt(document.getElementById("set-price").value);
        if (!setCount) {
            return true; // If no set count selected, skip validation
        }


        let itemCount = 0;

        // Count items that have at least one field filled
        const sections = ["top", "bottom", "coat", "other", "top2"];
        sections.forEach((section) => {
            const type = document.getElementById(`${section}-type`);
            if (type && type.value) itemCount++;
        });

        if (itemCount < setCount) {
            showValidationModal(
                `Bạn chọn ${setCount} sản phẩm nhưng chỉ nhập ${itemCount}.\nVui lòng nhập thêm ${setCount - itemCount
                } món nữa.`
            );
            return false;
        }
    }
    return true;
}
// Hàm xóa dữ liệu trong các input
function clearInputs() {
    // Lấy tất cả các input type="text" và đặt giá trị về trống
    document.querySelectorAll('input[type="text"]').forEach(input => input.value = '');

    // Lấy tất cả các select và đặt giá trị về mặc định (giá trị đầu tiên)
    document.querySelectorAll('select').forEach(select => select.selectedIndex = 0);
}

// Add this new function
function showValidationModal(message) {
    // Create modal container
    const modalContainer = document.createElement('div');
    modalContainer.className = 'validation-modal-container';

    // Create modal content
    const modalContent = document.createElement('div');
    modalContent.className = 'validation-modal-content';

    // Add message
    const messageElement = document.createElement('p');
    messageElement.textContent = message;

    // Add close button
    const closeButton = document.createElement('button');
    closeButton.className = 'btn btn-primary';
    closeButton.textContent = 'Ok';
    closeButton.onclick = () => modalContainer.remove();

    // Assemble modal
    modalContent.appendChild(messageElement);
    modalContent.appendChild(closeButton);
    modalContainer.appendChild(modalContent);
    document.body.appendChild(modalContainer);
}

// Hàm xử lý tạo kết quả
document.getElementById('generate-output').addEventListener('click', function () {
    // Get set quantity
    const setPrice = document.getElementById("set-price").value;
    const priceInput = document.getElementById("price-input").value;
    // Get current template type from visible sections
    const isShoeTemplate = document.getElementById("shoes-section").style.display === 'block';
    const isAccessoryTemplate = document.getElementById("accessories-section").style.display === 'block';
    const isBagTemplate = document.getElementById("bags-section").style.display === 'block';

    // Get clothes type if it chose
    const isTopChose = document.getElementById("top-type").value != null;
    const isTop2Chose = document.getElementById("top2-type")?.value != null;
    const isBottomChose = document.getElementById("bottom-type").value != null;
    const isCoatChose = document.getElementById("coat-type").value != null;
    const isOtherChose = document.getElementById("other-name")?.value != null;

    // Only validate set price for clothing template
    if ((!isShoeTemplate || !isAccessoryTemplate || !isBagTemplate) && !setPrice) {
        showValidationModal("Vui lòng chọn số lượng sản phẩm!");
        return;
    }

    if (!validateInputs()) return; // Stop if validation fails

    let resultDisplay = "";
    // Add set quantity
    if (setPrice !== null && setPrice > 0 && priceInput > 0) {
        resultDisplay += getSetQuantity(setPrice);
        resultDisplay += getPrice(priceInput);
    }
    else {
        resultDisplay += getSetQuantity(setPrice) + "\n";
    }

    if (setPrice == "1") {
        try {
            const singleName = document.getElementById("single-item-name")?.value || null;
            const fitSingle = document.getElementById("single-item-fit")?.value || null;
            const singleChest = document.getElementById("single-item-chest")?.value || null;
            const singleButt = document.getElementById("single-item-butt")?.value || null;
            const singleWaist = document.getElementById("single-item-waist")?.value || null;
            const singleHip = document.getElementById("single-item-hip")?.value || null;
            const singleLength = document.getElementById("single-item-length")?.value || null;
            const singleArmpit = document.getElementById("single-item-armpit")?.value || null;
            const singleThigh = document.getElementById("single-item-thigh")?.value || null;
            const singleDefect = document.getElementById("single-item-defect")?.value || null;

            resultDisplay += getSingleInfo(
                singleName,
                fitSingle,
                singleChest,
                singleButt,
                singleWaist,
                singleHip,
                singleLength,
                singleArmpit,
                singleThigh,
                singleDefect
            );
        } catch (error) {
            console.error("Error processing single item data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu. Vui lòng thử lại. / An error occurred while processing data. Please try again.");
            return;
        }
    }
    if (isTopChose) {
        try {
            const topType = document.getElementById("top-type").value;
            const fitTop = document.getElementById("fit-top").value;
            const topChest = document.getElementById("top-chest").value;
            const topWaist = document.getElementById("top-waist").value;
            const topLength = document.getElementById("top-length").value;
            const topArmpit = document.getElementById("top-armpit").value;
            const topDefect = document.getElementById("top-defect").value;

            resultDisplay += getTopInfo(
                topType,
                fitTop,
                topChest,
                topWaist,
                topLength,
                topArmpit,
                topDefect
            );
        } catch (error) {
            console.error("Error processing top item data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu áo. / Error processing top data.");
            return;
        }
    }
    if (isTop2Chose) {
        try {
            const top2Type = document.getElementById("top2-type")?.value || null;
            const fit2Top = document.getElementById("fit2-top")?.value || null;
            const top2Chest = document.getElementById("top2-chest")?.value || null;
            const top2Waist = document.getElementById("top2-waist")?.value || null;
            const top2Length = document.getElementById("top2-length")?.value || null;
            const top2Armpit = document.getElementById("top2-armpit")?.value || null;
            const top2Defect = document.getElementById("top2-defect")?.value || null;

            resultDisplay += getTop2Info(
                top2Type,
                fit2Top,
                top2Chest,
                top2Waist,
                top2Length,
                top2Armpit,
                top2Defect
            );
        } catch (error) {
            console.error("Error processing second top item data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu áo thứ hai. / Error processing second top data.");
            return;
        }
    }
    if (isBottomChose) {
        try {
            const bottomType = document.getElementById("bottom-type").value;
            const fitBottom = document.getElementById("fit-bottom").value;
            const bottomHip = document.getElementById("bottom-hip").value;
            const bottomWaist = document.getElementById("bottom-waist").value;
            const bottomLength = document.getElementById("bottom-length").value;
            const bottomThigh = document.getElementById("bottom-thigh").value;
            const bottomDefect = document.getElementById("bottom-defect").value;

            resultDisplay += getBottomInfo(
                bottomType,
                fitBottom,
                bottomHip,
                bottomWaist,
                bottomLength,
                bottomThigh,
                bottomDefect
            );
        } catch (error) {
            console.error("Error processing bottom item data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu quần. / Error processing bottom data.");
            return;
        }
    }
    if (isCoatChose) {
        try {
            const coatType = document.getElementById("coat-type").value;
            const fitCoat = document.getElementById("fit-coat").value;
            const coatArmpit = document.getElementById("coat-armpit").value;
            const coatLength = document.getElementById("coat-length").value;
            const coatDefect = document.getElementById("coat-defect").value;

            resultDisplay += getCoatInfo(
                coatType,
                fitCoat,
                coatArmpit,
                coatLength,
                coatDefect
            );
        } catch (error) {
            console.error("Error processing coat item data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu áo khoác. / Error processing coat data.");
            return;
        }
    }

    if (isOtherChose) {
        try {
            const otherName = document.getElementById("other-name")?.value || null;
            const fitOther = document.getElementById("other-fit")?.value || null;
            const otherChest = document.getElementById("other-chest")?.value || null;
            const otherButt = document.getElementById("other-butt")?.value || null;
            const otherWaist = document.getElementById("other-waist")?.value || null;
            const otherHip = document.getElementById("other-hip")?.value || null;
            const otherLength = document.getElementById("other-length")?.value || null;
            const otherArmpit = document.getElementById("other-armpit")?.value || null;
            const otherThigh = document.getElementById("other-thigh")?.value || null;
            const otherDefect = document.getElementById("other-defect")?.value || null;

            resultDisplay += getOtherInfo(
                otherName,
                fitOther,
                otherChest,
                otherButt,
                otherWaist,
                otherHip,
                otherLength,
                otherArmpit,
                otherThigh,
                otherDefect
            );
        } catch (error) {
            console.error("Error processing other item data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu sản phẩm khác. / Error processing other item data.");
            return;
        }
    }
    
    if (isShoeTemplate) {
        try {
            const shoeType = document.getElementById("shoe-type").value;
            const shoeSize = document.getElementById("shoe-size").value;
            const shoeLength = document.getElementById("shoe-length").value;
            const shoeWidth = document.getElementById("shoe-width").value;
            const shoeDefect = document.getElementById("shoe-defect").value;

            resultDisplay += getShoesInfo(shoeType, shoeSize, shoeLength, shoeWidth, shoeDefect);
        } catch (error) {
            console.error("Error processing shoe data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu giày. / Error processing shoe data.");
            return;
        }
    }
    
    if (isAccessoryTemplate) {
        try {
            const accessoryType = document.getElementById("accessory-type").value;
            const accessorySize = document.getElementById("accessory-size").value;
            const accessoryLength = document.getElementById("accessory-length").value;
            const accessoryWidth = document.getElementById("accessory-width").value;
            const accessoryDefect = document.getElementById("accessory-defect").value;

            resultDisplay += getAccessoriesInfo(accessoryType, accessorySize, accessoryLength, accessoryWidth, accessoryDefect);
        } catch (error) {
            console.error("Error processing accessory data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu phụ kiện. / Error processing accessory data.");
            return;
        }
    }

    if (isBagTemplate) {
        try {
            const bagType = document.getElementById("bag-type").value;
            const bagSize = document.getElementById("bag-size").value;
            const bagLength = document.getElementById("bag-length").value;
            const bagWidth = document.getElementById("bag-width").value;
            const bagHeight = document.getElementById("bag-height").value;
            const bagDefect = document.getElementById("bag-defect").value;

            resultDisplay += getBagsInfo(bagType, bagSize, bagLength, bagWidth, bagHeight, bagDefect);
        } catch (error) {
            console.error("Error processing bag data:", error);
            showValidationModal("Có lỗi xảy ra khi xử lý dữ liệu túi. / Error processing bag data.");
            return;
        }
    }

    document.getElementById("output").textContent = resultDisplay;

    resultDisplay += getAttentionMessage(currentLanguage);

    addToHistory(resultDisplay);

    // Xóa dữ liệu input
    clearInputs();

    // Clear the start-range input after use
    document.getElementById('start-range').value = '';
});



// Translation object
const translations = {
    chest: {
        en: "Chest",
        vi: "Ngực",
        both: "Ngực / Chest"
    },
    waist: {
        en: "Waist",
        vi: "Eo",
        both: "Eo / Waist"
    },
    length: {
        en: "Length",
        vi: "Dài",
        both: "Dài / Length"
    },
    armpit: {
        en: "Armpit",
        vi: "Vòng nách",
        both: "Vòng nách / Armpit"
    },
    hip: {
        en: "Hip",
        vi: "Hông",
        both: "Hông / Hip"
    },
    butt: {
        en: "Butt",
        vi: "Mông",
        both: "Mông / Butt"
    },
    thigh: {
        en: "Thigh",
        vi: "Đùi",
        both: "Đùi / Thigh"
    },
    width: {
        en: "Width",
        vi: "Rộng",
        both: "Rộng / Width"
    },
    height: {
        en: "Height",
        vi: "Cao",
        both: "Cao / Height"
    }
};

function getPrice(priceInput) {
    try {
        // Remove 'k' and convert to number
        const vndAmount = parseFloat(priceInput.replace('k', ''));
        
        // Convert to USD (divide by 23)
        const usdAmount = vndAmount / 23;
        
        // Round to 1 decimal place
        const roundedUSD = Math.round(usdAmount * 10) / 10;
        
        // Format the output
        return `${priceInput}k / ${roundedUSD}usd \n`;
    } catch (error) {
        console.error("Error processing price:", error);
        showValidationModal("Có lỗi xảy ra khi xử lý giá tiền. / Error processing price.");
        return "";
    }
}

function getSetQuantity(setPrice) {
    const messages = {
        1: "✨\n🎀𝐏𝐫𝐢𝐜𝐞: ",
        2: "✨\n🎀𝐒𝐞𝐭 𝐩𝐫𝐢𝐜𝐞 𝟐𝐩𝐜𝐬: ",
        3: "✨\n🎀𝐒𝐞𝐭 𝐩𝐫𝐢𝐜𝐞 𝟑𝐩𝐜𝐬: ",
        4: "✨\n🎀𝐒𝐞𝐭 𝐩𝐫𝐢𝐜𝐞 𝟒𝐩𝐜𝐬: ",
    };

    return messages[setPrice];
}
// Helper function to get translated text
const getTranslatedText = (key) => translations[key][currentLanguage] || translations[key]['both'];

// Modified getTopInfo function
const getTopInfo = (topType, fitTop, topChest, topWaist, topLength, topArmpit, topDefect) => {
    var result = "";

    if (topType) {
        result += `${convertToBoldUnicode(topType)}:\n`;
        if (topDefect) result += `${topDefect}\n`;
        if (fitTop) result += `  - Fit: ${fitTop}\n`;
        if (topChest) result += `  - ${getTranslatedText('chest')}: ${topChest}cm\n`;
        if (topWaist) result += `  - ${getTranslatedText('waist')}: ${topWaist}cm\n`;
        if (topLength) result += `  - ${getTranslatedText('length')}: ${topLength}cm\n`;
        if (topArmpit) result += `  - ${getTranslatedText('armpit')}: ${topArmpit}cm\n`;
    }

    return result;
}

const getTop2Info = (top2Type, fit2Top, top2Chest, top2Waist, top2Length, top2Armpit, top2Defect) => {
    var result = "";

    // Kiểm tra nếu top2Type không phải là chuỗi rỗng để thêm vào hoặc không
    if (top2Type) {

        // Add type    
        result += `${convertToBoldUnicode(top2Type)}:\n`;
        if (top2Defect) result += `${top2Defect}\n`;

        // Edit Freesize option
        if (fit2Top) {
            if (fit2Top != "Freesize") result += `  - Fit: ${fit2Top}\n`;
            else result += `  - Freesize\n`;
        }

        // Add other things
        if (top2Chest)
            result += `  - ${getTranslatedText("chest")}: ${top2Chest}cm\n`;
        if (top2Waist)
            result += `  - ${getTranslatedText("waist")}: ${top2Waist}cm\n`;
        if (top2Length)
            result += `  - ${getTranslatedText("length")}: ${top2Length}cm\n`;
        if (top2Armpit)
            result += `  - ${getTranslatedText("armpit")}: ${top2Armpit}cm\n`;
    }

    return result;
}

// Add Bottom information
const getBottomInfo = (bottomType, fitBottom, bottomHip, bottomWaist, bottomLength, bottomThigh, bottomDefect) => {
    var result = "";

    // Kiểm tra nếu top2Type không phải là chuỗi rỗng để thêm vào hoặc không
    if (bottomType) {

        // Add type  
        result += `${convertToBoldUnicode(bottomType)}:\n`;

        if (bottomDefect) result += `${bottomDefect}\n`;

        // Edit Freesize option
        if (fitBottom) {
            if (fitBottom != "Freesize") result += `  - Fit: ${fitBottom}\n`;
            else result += `  - Freesize\n`;
        }

        if(bottomHip) {
            result += `  - ${getTranslatedText("hip")}: ${bottomHip}cm\n`;
        }
        if (bottomWaist)
            result += `  - ${getTranslatedText("waist")}: ${bottomWaist}cm\n`;
        if (bottomLength)
            result += `  - ${getTranslatedText("length")}: ${bottomLength}cm\n`;
        if (bottomThigh)
            result += `  - ${getTranslatedText("thigh")}: ${bottomThigh}cm\n`;
    }

    return result;
}

//  Add Coat information
const getCoatInfo = (coatType, fitCoat, coatArmpit, coatLength, coatDefect) => {
    var result = "";

    // Kiểm tra nếu top2Type không phải là chuỗi rỗng để thêm vào hoặc không
    if (coatType) {
        // Add type
        result += `${convertToBoldUnicode(coatType)}:\n`;

        if (coatDefect) result += `${coatDefect}\n`;

        // Edit Freesize option
        if (fitCoat) {
            if (fitCoat != "Freesize") result += `  - Fit: ${fitCoat}\n`;
            else result += `  - Freesize\n`;
        }

        // Add other things
        if (coatArmpit)
            result += `  - ${getTranslatedText("armpit")}: ${coatArmpit}cm\n`;
        if (coatLength)
            result += `  - ${getTranslatedText("length")}: ${coatLength}cm\n`;
    }
    return result;
};



const getOtherInfo = (otherName, fitOther, otherChest, otherButt, otherWaist, otherHip, otherLength, otherArmpit, otherThigh, otherDefect) => {
    var result = "";

    if (otherName) {
        // Add type
        result += `${convertToBoldUnicode(otherName)}:\n`;

        if (otherDefect) result += `${otherDefect}\n`;

        // Edit Freesize option
        if (fitOther) {
            if (fitOther != "Freesize") result += `  - Fit: ${fitOther}\n`;
            else result += `  - Freesize\n`;
        }

        if (otherChest)
            result += `  - ${getTranslatedText("chest")}: ${otherChest}cm\n`;
        if (otherButt)
            result += `  - ${getTranslatedText("butt")}: ${otherButt}cm\n`;
        if (otherWaist)
            result += `  - ${getTranslatedText("waist")}: ${otherWaist}cm\n`;
        if (otherHip)
            result += `  - ${getTranslatedText("hip")}: ${otherHip}cm\n`;
        if (otherLength)
            result += `  - ${getTranslatedText("length")}: ${otherLength}cm\n`;
        if (otherArmpit)
            result += `  - ${getTranslatedText("armpit")}: ${otherArmpit}cm\n`;
        if (otherThigh)
            result += `  - ${getTranslatedText("thigh")}: ${otherThigh}cm\n`;
    }

    return result;
}

// Add Single item information
const getSingleInfo = (fitsingle, singleChest, singleButt, singleWaist, singleHip, singleLength, singleArmpit, singleThigh, singleDefect) => {
    var result = "";


    if (singleDefect) result += `${singleDefect}\n`;

    // Edit Freesize option
    if (fitsingle) {
        if (fitsingle != "Freesize") result += `  - Fit: ${fitsingle}\n`;
        else result += `  - Freesize\n`;
    }

    if (singleChest)
        result += `  - ${getTranslatedText("chest")}: ${singleChest}cm\n`;
    if (singleButt)
        result += `  - ${getTranslatedText("butt")}: ${singleButt}cm\n`;
    if (singleWaist)
        result += `  - ${getTranslatedText("waist")}: ${singleWaist}cm\n`;
    if (singleHip) result += `  - ${getTranslatedText("hip")}: ${singleHip}cm\n`;
    if (singleLength)
        result += `  - ${getTranslatedText("length")}: ${singleLength}cm\n`;
    if (singleArmpit)
        result += `  - ${getTranslatedText("armpit")}: ${singleArmpit}cm\n`;
    if (singleThigh)
        result += `  - ${getTranslatedText("thigh")}: ${singleThigh}cm\n`;

    return result;
}

// Get Shoes information
const getShoesInfo = (shoeType, shoeSize, shoeLength, shoeWidth, shoeDefect) => {
    var result = "";

    if (shoeType) {
        // Add type
        result += `${convertToBoldUnicode(shoeType)}:\n`;

        if (shoeDefect) result += `${shoeDefect}\n`;

        // Add size
        if (shoeSize) result += `  - Size: ${shoeSize}\n`;

        // Add measurements
        if (shoeLength)
            result += `  - ${getTranslatedText("length")}: ${shoeLength}cm\n`;
        if (shoeWidth)
            result += `  - ${getTranslatedText("width")}: ${shoeWidth}cm\n`;
    }
    return result;
};

// Get Accessories information
const getAccessoriesInfo = (accessoryType, accessorySize, accessoryLength, accessoryWidth, accessoryDefect) => {
    var result = "";

    if (accessoryType) {
        // Add type
        result += `${convertToBoldUnicode(accessoryType)}:\n`;

        if (accessoryDefect) result += `${accessoryDefect}\n`;

        // Add size if not freesize
        if (accessorySize) {
            if (accessorySize !== "Freesize") result += `  - Size: ${accessorySize}\n`;
            else result += `  - Freesize\n`;
        }

        // Add measurements
        if (accessoryLength)
            result += `  - ${getTranslatedText("length")}: ${accessoryLength}cm\n`;
        if (accessoryWidth)
            result += `  - ${getTranslatedText("width")}: ${accessoryWidth}cm\n`;
    }
    return result;
};

// Get Bags information
const getBagsInfo = (bagType, bagSize, bagLength, bagWidth, bagHeight, bagDefect) => {
    var result = "";

    if (bagType) {
        // Add type
        result += `${convertToBoldUnicode(bagType)}:\n`;

        if (bagDefect) result += `${bagDefect}\n`;

        // Add size
        if (bagSize) result += `  - Size: ${bagSize}\n`;

        // Add measurements
        if (bagLength)
            result += `  - ${getTranslatedText("length")}: ${bagLength}cm\n`;
        if (bagWidth)
            result += `  - ${getTranslatedText("width")}: ${bagWidth}cm\n`;
        if (bagHeight)
            result += `  - ${getTranslatedText("height")}: ${bagHeight}cm\n`;
    }
    return result;
};

// Hàm sao chép kết quả
var copyOutput = document.getElementById('copy-output')
copyOutput.addEventListener('click', function () {
    copyToClipboardWithIndex(document.getElementById('output').textContent, 'Kết quả đã được sao chép!');
});


let historyCount = 0;
// Thêm kết quả vào lịch sử
function addToHistory(resultDisplay) {
    let startRange = document.getElementById('start-range').value;
    if(startRange && parseInt(startRange) > 0) {
        historyCount = parseInt(startRange) - 1;
    }
    historyCount++;
    const historyList = document.getElementById('history-list');

    const historyItem = document.createElement('li');
    historyItem.className = 'list-group-item';
    historyItem.dataset.index = historyCount;

    historyItem.innerHTML = `
        <strong>#${historyCount}:</strong>
        <pre class="history-content">${resultDisplay}</pre>
        <div class="btn-group">
            <button class="btn btn-sm btn-outline-success me-2 rounded-pill px-3 py-2" onclick="copyHistoryItem(${historyCount})">
                <i class="bi bi-clipboard"></i> Sao chép
            </button>
            <button class="btn btn-sm btn-outline-warning me-2 rounded-pill px-3 py-2" onclick="editHistory(${historyCount})">
                <i class="bi bi-pencil-square"></i> Sửa
            </button>
            <button class="btn btn-sm btn-outline-danger me-2 rounded-pill px-3 py-2" onclick="deleteHistory(${historyCount})">
                <i class="bi bi-trash"></i> Xóa
            </button>
        </div>
        <div class="edit-container d-none mt-2">
            <textarea class="form-control mb-2 inter-body">${resultDisplay}</textarea>
            <div class="btn-group">
                <button class="btn btn-sm btn-outline-primary me-2 rounded-pill px-3 py-2" onclick="saveEdit(${historyCount})">Lưu</button>
                <button class="btn btn-sm btn-outline-secondary rounded-pill px-3 py-2" onclick="cancelEdit(${historyCount})">Hủy</button>
            </div>
        </div>
    `;

    historyList.appendChild(historyItem);
    
    // Sort history items after adding new item
    sortHistoryItems();
}

// Sao chép từng mục lịch sử
function copyHistoryItem(index) {
    const historyItem = document.querySelector(`#history-list li[data-index="${index}"]`);
    if (historyItem) {
        const message = `History #${index} has been copied! / Lịch sử #${index} đã được sao chép!`;
        copyToClipboardWithIndex(historyItem.querySelector('.history-content').textContent, message, index);
    }
}

// Xóa lịch sử
function deleteHistory(index) {
    const itemToRemove = document.querySelector(`#history-list li[data-index="${index}"]`);
    if (itemToRemove) {
        itemToRemove.remove();
        updateHistoryNumbers();
    }
}

// Sao chép toàn bộ lịch sử
document.getElementById('copy-history').addEventListener('click', function () {
    const allHistoryItems = document.querySelectorAll('#history-list li pre');
    const historyText = Array.from(allHistoryItems).map((item, index) => `#${index + 1}:\n${item.textContent}`).join('\n\n');
    copyToClipboardWithIndex(historyText, 'Toàn bộ lịch sử đã được sao chép!');
});

// Hàm sao chép vào clipboard với số thứ tự
function copyToClipboardWithIndex(text, successMessage, index) {
    const tempTextArea = document.createElement('textarea');
    tempTextArea.value = `#${index}:\n${text}`;
    document.body.appendChild(tempTextArea);
    tempTextArea.select();
    document.execCommand('copy');
    document.body.removeChild(tempTextArea);
    alert(successMessage);
}

// Cập nhật số thứ tự sau khi xóa
function updateHistoryNumbers() {
    const allItems = document.querySelectorAll('#history-list li');
    historyCount = 0; // Reset lại số thứ tự
    allItems.forEach(item => {
        historyCount++;
        item.dataset.index = historyCount;
        const strongTag = item.querySelector('strong');
        if (strongTag) strongTag.textContent = `#${historyCount}:`;
    });
}

// Add event listener to Additional Top
document.addEventListener("DOMContentLoaded", () => {
    const topTypeElement = document.getElementById("top-type");
    const generateOutputButton = document.getElementById("generate-output");
    const additionalHtml = document.getElementById("additional-html");

    additionalHtml.style.display = "none"; // Hide by default

    // Function to show or hide the additional HTML section
    const toggleAdditionalHtml = () => {
        const selectedValue = topTypeElement.value;

        if (selectedValue === "Inner Top" || selectedValue === "Outer Top") {
            additionalHtml.style.display = "flex"; // Show the additional HTML
        } else {
            additionalHtml.style.display = "none"; // Hide the additional HTML
        }
    };

    // Listen for changes in the top-type dropdown
    topTypeElement.addEventListener("change", toggleAdditionalHtml);

    // Hide the additional HTML when the "Generate Output" button is pressed
    generateOutputButton.addEventListener("click", () => {
        additionalHtml.style.display = "none"; // Hide the additional HTML
    });
});


// Get the "Add Section" button and the hidden section
const addSectionButton = document.getElementById('add-section');
const otherSection = document.getElementById('new-section');

// Listen for click event on "Add Section" button
addSectionButton.addEventListener('click', function () {
    // Show the hidden section
    otherSection.style.display = 'block';
    // Hide the "Add Section" button
    addSectionButton.style.display = 'none';
});

// Function to hide section
function deleteSection(button) {
    const sectionToHide = button.closest('section');
    // Hide the section
    sectionToHide.style.display = 'none';

    // Clear all inputs in the hidden section
    const inputs = sectionToHide.querySelectorAll('input, select');
    inputs.forEach(input => {
        input.value = '';
    });

    // Show the "Add Section" button again
    addSectionButton.style.display = 'inline-block';
}

// Chuyển đổi phông chữ trực tiếp
function convertToBoldUnicode(inputText) {
    if (!inputText) return '';  // Return an empty string if input is empty

    // Mapping for converting normal characters to bold unicode
    const boldMap = {
        'A': '𝐀', 'B': '𝐁', 'C': '𝐂', 'D': '𝐃', 'E': '𝐄', 'F': '𝐅', 'G': '𝐆', 'H': '𝐇', 'I': '𝐈', 'J': '𝐉',
        'K': '𝐊', 'L': '𝐋', 'M': '𝐌', 'N': '𝐍', 'O': '𝐎', 'P': '𝐏', 'Q': '𝐐', 'R': '𝐑', 'S': '𝐒', 'T': '𝐓',
        'U': '𝐔', 'V': '𝐕', 'W': '𝐖', 'X': '𝐗', 'Y': '𝐘', 'Z': '𝐙',
        'a': '𝐚', 'b': '𝐛', 'c': '𝐜', 'd': '𝐝', 'e': '𝐞', 'f': '𝐟', 'g': '𝐠', 'h': '𝐡', 'i': '𝐢', 'j': '𝐣',
        'k': '𝐤', 'l': '𝐥', 'm': '𝐦', 'n': '𝐧', 'o': '𝐨', 'p': '𝐩', 'q': '𝐪', 'r': '𝐫', 's': '𝐬', 't': '𝐭',
        'u': '𝐮', 'v': '𝐯', 'w': '𝐰', 'x': '𝐱', 'y': '𝐲', 'z': '𝐳',
        '0': '𝟎', '1': '𝟏', '2': '𝟐', '3': '𝟑', '4': '𝟒', '5': '𝟓', '6': '𝟔', '7': '𝟕', '8': '𝟖', '9': '𝟗',

        // Vietnamese uppercase characters
        'À': '𝐀̀', 'Á': '𝐀́', 'Ả': '𝐀̉', 'Ã': '𝐀̃', 'Ạ': '𝐀̣',
        'È': '𝐄̀', 'É': '𝐄́', 'Ẻ': '𝐄̉', 'Ẽ': '𝐄̃', 'Ẹ': '𝐄̣',
        'Ì': '𝐼̀', 'Í': '𝐼́', 'Ỉ': '𝐼̉', 'Ĩ': '𝐼̃', 'Ị': '𝐼̣',
        'Ò': '𝑂̀', 'Ó': '𝑂́', 'Ỏ': '𝑂̉', 'Õ': '𝑂̃', 'Ọ': '𝑂̣',
        'Ù': '𝑈̀', 'Ú': '𝑈́', 'Ủ': '𝑈̉', 'Ũ': '𝑈̃', 'Ụ': '𝑈̣',
        'Ỳ': '𝑌̀', 'Ý': '𝑌́', 'Ỷ': '𝑌̉', 'Ỹ': '𝑌̃', 'Ỵ': '𝑌̣',
        'Đ': '𝐷', 'Ê': '𝐸̂', 'Ô': '𝑂̂', 'Ơ': '𝑂̛', 'Ư': '𝑈̛',

        // Vietnamese lowercase characters
        'à': '𝐚̀', 'á': '𝐚́', 'ả': '𝐚̉', 'ã': '𝐚̃', 'ạ': '𝐚̣',
        'è': '𝐞̀', 'é': '𝐞́', 'ẻ': '𝐞̉', 'ẽ': '𝐞̃', 'ẹ': '𝐞̣',
        'ì': '𝑖̀', 'í': '𝑖́', 'ỉ': '𝑖̉', 'ĩ': '𝑖̃', 'ị': '𝑖̣',
        'ò': '𝑜̀', 'ó': '𝑜́', 'ỏ': '𝑜̉', 'õ': '𝑜̃', 'ọ': '𝑜̣',
        'ù': '𝑢̀', 'ú': '𝑢́', 'ủ': '𝑢̉', 'ũ': '𝑢̃', 'ụ': '𝑢̣',
        'ỳ': '𝑦̀', 'ý': '𝑦́', 'ỷ': '𝑦̉', 'ỹ': '𝑦̃', 'ỵ': '𝑦̣',
        'đ': '𝑑', 'ê': '𝑒̂', 'ô': '𝑜̂', 'ơ': '𝑜̛', 'ư': '𝑢̛',
    };

    const boldText = Array.from(inputText).map(char => boldMap[char] || char).join('');
    return boldText;
}

// Add these new functions for editing functionality
function editHistory(index) {
    const historyItem = document.querySelector(`#history-list li[data-index="${index}"]`);
    const editContainer = historyItem.querySelector('.edit-container');
    const content = historyItem.querySelector('.history-content');

    editContainer.classList.remove('d-none');
    content.classList.add('d-none');
}

function saveEdit(index) {
    const historyItem = document.querySelector(`#history-list li[data-index="${index}"]`);
    const editContainer = historyItem.querySelector('.edit-container');
    const content = historyItem.querySelector('.history-content');
    const textarea = editContainer.querySelector('textarea');

    content.textContent = textarea.value;
    editContainer.classList.add('d-none');
    content.classList.remove('d-none');
}

function cancelEdit(index) {
    const historyItem = document.querySelector(`#history-list li[data-index="${index}"]`);
    const editContainer = historyItem.querySelector('.edit-container');
    const content = historyItem.querySelector('.history-content');
    const textarea = editContainer.querySelector('textarea');

    textarea.value = content.textContent;
    editContainer.classList.add('d-none');
    content.classList.remove('d-none');
}

const getAttentionMessage = (language) => {
    const messages = {
        attention: {
            en: `${convertToBoldUnicode("!!Attention!!")}`,
            vi: `${convertToBoldUnicode("!!Lưu ý!!")}`,
            both: `${convertToBoldUnicode("!!Attention!!")} / ${convertToBoldUnicode("!!Lưu ý!!")}️`
        },
        priority: {
            en: `${convertToBoldUnicode("Payment")}: Payment within 12 hours.`,
            vi: `${convertToBoldUnicode("Thanh toán")}: Thanh toán trong vòng 12 tiếng.`,
            both: `${convertToBoldUnicode("Payment")}/ ${convertToBoldUnicode("Thanh toán")}: Payment within 12 hours / Thanh toán trong vòng 12 tiếng.`
        },
        details: {
            en: `${convertToBoldUnicode("Product Details")}: Check each post carefully before buying.`,
            vi: `${convertToBoldUnicode("Thông tin sản phẩm")}: Vui lòng đọc kỹ bài đăng trước khi mua.`,
            both: `${convertToBoldUnicode("Product Details")} / ${convertToBoldUnicode("Thông tin sản phẩm")}: Check each post carefully before buying / Vui lòng đọc kỹ bài đăng trước khi mua.`
        },
        secondhand: {
            en: `${convertToBoldUnicode("Secondhand Items")}: May have minor flaws not visible in pictures.`,
            vi: `${convertToBoldUnicode("Hàng secondhand")}: Có thể có khuyết điểm nhỏ không thấy trong ảnh.`,
            both: `${convertToBoldUnicode("Secondhand Items")} / ${convertToBoldUnicode("Hàng secondhand")}: May have minor flaws not visible in pictures / Có thể có khuyết điểm nhỏ không thấy trong ảnh.`
        },
        unboxing: {
            en: `${convertToBoldUnicode("Unboxing")}: Record a video when opening the package.`,
            vi: `${convertToBoldUnicode("Mở hàng")}: Quay video khi mở hàng.`,
            both: `${convertToBoldUnicode("Unboxing")} / ${convertToBoldUnicode("Mở hàng")}: Record a video when opening the package / Quay video khi mở hàng.`
        },
        noReturn: {
            en: `${convertToBoldUnicode("No Return / Refund")}: Except for serious defects with unboxing video proof.`,
            vi: `${convertToBoldUnicode("Không hoàn trả")}: Trừ trường hợp lỗi nghiêm trọng có video mở hàng.`,
            both: `${convertToBoldUnicode("No Return / Refund")} / ${convertToBoldUnicode("Không hoàn trả")}: Except for serious defects with unboxing video proof / Trừ trường hợp lỗi nghiêm trọng có video mở hàng.`
        }
    };

    return `${messages.attention[language]}
            ${messages.priority[language]}
            ${messages.details[language]}
            ${messages.secondhand[language]}
            ${messages.unboxing[language]}
            ${messages.noReturn[language]}`;
};


// Add event listener to the set-price dropdown
document.getElementById('set-price').addEventListener('change', function() {
    const selectedValue = this.value;
    const currentTemplate = getCurrentTemplate();

    // Only process set-price changes for clothing template
    if (currentTemplate !== 'clothing') {
        return;
    }

    // Get all clothing-related sections
    const clothingSections = [
        document.getElementById('single-item'),
        ...document.querySelectorAll('section:has([id$="-type"])')
    ].filter(section => 
        section && 
        !section.id.includes('shoes') && 
        !section.id.includes('accessories') && 
        !section.id.includes('bags')
    );

    if (selectedValue === "1") {
        // For single item, show only single-item section
        clothingSections.forEach(section => {
            section.style.display = section.id === 'single-item' ? 'block' : 'none';
        });
    } else if (["2", "3", "4"].includes(selectedValue)) {
        // For multiple items, show all clothing sections except single-item
        clothingSections.forEach(section => {
            section.style.display = section.id === 'single-item' ? 'none' : 'block';
        });
    }
});


// Add an input event listener to catch all changes (recommended)
document.getElementById('set-price').addEventListener('input', function() {
    // Trigger the change event manually
    const event = new Event('change');
    this.dispatchEvent(event);
});

// Helper function to determine current template
function getCurrentTemplate() {
    if (document.getElementById('single-item').style.display === 'block' ||
        document.getElementById('set-number').style.display === 'block') {
        return 'clothing';
    }
    if (document.getElementById('accessories-section').style.display === 'block') {
        return 'accessories';
    }
    if (document.getElementById('shoes-section').style.display === 'block') {
        return 'shoes';
    }
    if (document.getElementById('bags-section').style.display === 'block') {
        return 'bags';
    }
    return null;
}

document.addEventListener('DOMContentLoaded', function () {
    const templateModal = document.getElementById('templateModal');
    const templateCards = document.querySelectorAll('.template-card');

    templateCards.forEach(card => {
        card.addEventListener('click', function () {
            const template = this.dataset.template;
            loadTemplate(template);
            templateModal.style.display = 'none';
        });
    });

    // Load clothing template by default
    loadTemplate('clothing');

    // Initially hide the modal
    templateModal.style.display = 'none';
});

function loadTemplate(template) {
    // Hide all sections initially
    const allSections = document.querySelectorAll('section');
    allSections.forEach(section => section.style.display = 'none');

    // Show set number section only for clothing template
    const setNumberSection = document.getElementById('set-number');
    setNumberSection.style.display = template === 'clothing' ? 'block' : 'none';

    // Reset set-price value
    const setPriceSelect = document.getElementById('set-price');
    setPriceSelect.value = '1';

    switch (template) {
        case 'clothing':
            // For clothing template, show single item section by default
            document.getElementById('single-item').style.display = 'block';
            // Hide other clothing sections initially
            ['top', 'bottom', 'coat'].forEach(section => {
                const elem = document.querySelector(`section:has(#${section}-type)`);
                if (elem) elem.style.display = 'none';
            });
            break;
        case 'accessories':
            document.getElementById('accessories-section').style.display = 'block';
            break;
        case 'shoes':
            document.getElementById('shoes-section').style.display = 'block';
            break;
        case 'bags':
            document.getElementById('bags-section').style.display = 'block';
            break;
    }

    // Trigger the set-price change event to ensure proper section visibility
    const event = new Event('change');
    setPriceSelect.dispatchEvent(event);
}

document.addEventListener('DOMContentLoaded', function () {
    const templateModal = document.getElementById('templateModal');
    const templateButton = document.getElementById('templateButton');
    const templateCards = document.querySelectorAll('.template-card');

    // Initially hide the modal
    templateModal.style.display = 'none';

    // Show modal when template button is clicked
    templateButton.addEventListener('click', function () {
        templateModal.style.display = 'flex';
    });

    // Hide modal when clicking outside the content
    templateModal.addEventListener('click', function (e) {
        if (e.target === templateModal) {
            templateModal.style.display = 'none';
        }
    });

    // Handle template selection
    templateCards.forEach(card => {
        card.addEventListener('click', function () {
            const template = this.dataset.template;
            loadTemplate(template);
            templateModal.style.display = 'none';

            // Hide set-number section for shoes, accessories, and bags
            const setNumberSection = document.getElementById('set-number');
            if (template === 'shoes' || template === 'accessories' || template === 'bags') {
                setNumberSection.style.display = 'none';
            } else {
                setNumberSection.style.display = 'block';
            }
        });
    });
});

// Add this new function to sort history items
function sortHistoryItems() {
    const historyList = document.getElementById('history-list');
    const items = Array.from(historyList.children);
    
    items.sort((a, b) => {
        const aIndex = parseInt(a.dataset.index);
        const bIndex = parseInt(b.dataset.index);
        return aIndex - bIndex;
    });
    
    // Clear the list and add back items in sorted order
    historyList.innerHTML = '';
    items.forEach(item => historyList.appendChild(item));
}